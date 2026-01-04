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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubClient {

    public static final String ERROR_USER_NOT_FOUND = "user not found";
    public static final String ERROR_UNABLE_TO_FETCH_REPOS = "unable to fetch repositories at this time";
    public static final String ERROR_UNABLE_TO_FETCH_BRANCHES = "unable to fetch branches at this time";

    private final RestClient gitHubClient;

    public @Nonnull List<GitHubRepository> getRepositoriesForUser(String username) {
        GitHubRepository[] repos = fetchUserRepos(username);

        assert repos != null;
        return List.of(repos);
    }

    private @Nullable GitHubRepository[] fetchUserRepos(String username) {
        try {
            return gitHubClient.get()
                    .uri("/users/{username}/repos", username)
                    .retrieve()
                    .body(GitHubRepository[].class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new GitHubClientException(HttpStatus.NOT_FOUND, ERROR_USER_NOT_FOUND);
        } catch (HttpServerErrorException ex) {
            throw new GitHubClientException(HttpStatus.BAD_GATEWAY, ERROR_UNABLE_TO_FETCH_REPOS);
        }
    }

    public @Nonnull List<GitHubBranch> getBranchesForRepo(GitHubRepository repo) {
        GitHubBranch[] branches = fetchBranchesForRepo(repo);

        assert branches != null;
        return List.of(branches);
    }

    private @Nullable GitHubBranch[] fetchBranchesForRepo(GitHubRepository repo) {
        try {
            return gitHubClient.get()
                    .uri("/repos/{owner}/{repo}/branches", repo.owner().login(), repo.name())
                    .retrieve()
                    .body(GitHubBranch[].class);
        } catch (HttpServerErrorException ex) {
            throw new GitHubClientException(HttpStatus.BAD_GATEWAY, ERROR_UNABLE_TO_FETCH_BRANCHES);
        }
    }
}
