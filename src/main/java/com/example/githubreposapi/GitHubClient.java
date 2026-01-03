package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubClient {

    private final RestClient gitHubClient;

    public @Nullable List<GitHubRepository> getRepositoriesForUser(String username) {
        GitHubRepository[] repos = fetchUserRepos(username);

        assert repos != null;
        return List.of(repos);
    }

    private GitHubRepository @Nullable [] fetchUserRepos(String username) {
        return gitHubClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .body(GitHubRepository[].class);
    }

    public List<GitHubBranch> getBranchesForRepo(GitHubRepository repo) {
        GitHubBranch[] branches = fetchBranchesForRepo(repo);

        assert branches != null;
        return List.of(branches);
    }

    private GitHubBranch[] fetchBranchesForRepo(GitHubRepository repo) {
        return gitHubClient.get()
                .uri("/repos/{owner}/{repo}/branches", repo.owner().login(), repo.name())
                .retrieve()
                .body(GitHubBranch[].class);
    }
}
