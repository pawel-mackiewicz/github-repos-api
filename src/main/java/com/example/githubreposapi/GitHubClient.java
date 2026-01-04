package com.example.githubreposapi;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubClient {

    private static final String ERROR_USER_NOT_FOUND = "user not found";
    private static final String ERROR_UNABLE_TO_FETCH_REPOS = "unable to fetch repositories at this time";
    private static final String ERROR_UNABLE_TO_FETCH_BRANCHES = "unable to fetch branches at this time";
    private static final String ERROR_RATE_LIMIT = "rate limit exceeded";
    private static final String ERROR_SERVER_TIMEOUT = "upstream service timeout";

    private final RestClient gitHubClient;

    public @Nonnull List<GitHubRepository> getRepositoriesForUser(String username) {
        GitHubRepository[] repos = fetchUserRepos(username);
        return repos != null ? List.of(repos) : Collections.emptyList();
    }

    public @Nonnull List<GitHubBranch> getBranchesForRepo(GitHubRepository repo) {
        GitHubBranch[] branches = fetchBranchesForRepo(repo);
        return branches != null ? List.of(branches) : Collections.emptyList();
    }

    private @Nullable GitHubRepository[] fetchUserRepos(String username) {
        try {
            var uri = String.format("/users/%s/repos", username);
            return getFromGitHubApi(uri, GitHubRepository[].class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new GitHubClientException(HttpStatus.NOT_FOUND, ERROR_USER_NOT_FOUND);
        } catch (HttpServerErrorException ex) {
            log.error("GitHub server error: {}", ERROR_UNABLE_TO_FETCH_REPOS, ex);
            throw new GitHubClientException(HttpStatus.BAD_GATEWAY, ERROR_UNABLE_TO_FETCH_REPOS);
        }
    }

    private @Nullable GitHubBranch[] fetchBranchesForRepo(GitHubRepository repo) {
        try {
            var uri = String.format("/repos/%s/%s/branches", repo.owner().login(), repo.name());
            return getFromGitHubApi(uri, GitHubBranch[].class);
        } catch (HttpServerErrorException ex) {
            log.error("GitHub server error: {}", ERROR_UNABLE_TO_FETCH_BRANCHES, ex);
            throw new GitHubClientException(HttpStatus.BAD_GATEWAY, ERROR_UNABLE_TO_FETCH_BRANCHES);
        }
    }

    private <T> T getFromGitHubApi(String uri, Class<T> responseType) {
        try {
            return gitHubClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(responseType);
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.warn("GitHub rate limit exceeded: {}", ERROR_RATE_LIMIT);
            throw new GitHubClientException(HttpStatus.TOO_MANY_REQUESTS, ERROR_RATE_LIMIT);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.REQUEST_TIMEOUT)) {
                log.warn("GitHub upstream service timeout: {}", ex.getMessage());
                throw new GitHubClientException(HttpStatus.GATEWAY_TIMEOUT, ERROR_SERVER_TIMEOUT);
            }
            throw ex;
        }
    }
}
