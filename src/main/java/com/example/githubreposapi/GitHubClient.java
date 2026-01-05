package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubClient {

    private static final String ERROR_USER_NOT_FOUND = "user not found";
    private static final String ERROR_RATE_LIMIT = "rate limit exceeded";
    private static final String ERROR_SERVER_TIMEOUT = "upstream service timeout";
    private static final String ERROR_UNABLE_TO_FETCH = "unable to fetch from upstream";

    private final RestClient gitHubClient;

    public @NonNull List<GitHubRepository> getRepositoriesForUser(String username) {
        GitHubRepository[] repos = fetchReposForUser(username);
        return repos != null ? List.of(repos) : Collections.emptyList();
    }

    public @NonNull List<GitHubBranch> getBranchesForRepo(GitHubRepository repo) {
        GitHubBranch[] branches = fetchBranchesForRepo(repo);
        return branches != null ? List.of(branches) : Collections.emptyList();
    }

    private @Nullable GitHubRepository[] fetchReposForUser(String username) {
        try {
            var uri = String.format("/users/%s/repos", username);
            return getFromGitHubApi(uri, GitHubRepository[].class);
        } catch (HttpClientErrorException.NotFound ex) {
            log.info("User {} not found on GitHub server", username);
            throw new GitHubClientException(HttpStatus.NOT_FOUND, ERROR_USER_NOT_FOUND, ex);
        }
    }

    private @Nullable GitHubBranch[] fetchBranchesForRepo(GitHubRepository repo) {
            var uri = String.format("/repos/%s/%s/branches", repo.owner().login(), repo.name());
            return getFromGitHubApi(uri, GitHubBranch[].class);
    }

    private @Nullable <T> T getFromGitHubApi(String uri, Class<T> responseType) {
        try {
            return gitHubClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(responseType);
        } catch (HttpClientErrorException.NotFound ex) {
            throw ex; // let NotFound propagate to be handled by caller
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.warn("GitHub rate limit exceeded: {}", ex.getMessage());
            throw new GitHubClientException(HttpStatus.TOO_MANY_REQUESTS, ERROR_RATE_LIMIT, ex);
        } catch (HttpStatusCodeException ex) {
            String errorMessage = ex.getStatusCode().equals(HttpStatus.REQUEST_TIMEOUT)
                    ? ERROR_SERVER_TIMEOUT
                    : ERROR_UNABLE_TO_FETCH;
            log.warn("GitHub error ({}): {}", ex.getStatusCode(), ex.getMessage());
            throw new GitHubClientException(HttpStatus.BAD_GATEWAY, errorMessage, ex);
        }
    }
}
