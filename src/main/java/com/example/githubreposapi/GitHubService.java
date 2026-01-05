package com.example.githubreposapi;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GitHubService {

    private final AsyncTaskExecutor executor;
    private final GitHubClient client;

    public GitHubService(
            @Qualifier("applicationTaskExecutor") AsyncTaskExecutor executor,
            GitHubClient client
    ) {
        this.executor = executor;
        this.client = client;
    }

    public @NonNull List<Repository> getUserRepositories(String username) {
        log.info("Fetching repositories for user: {}", username);
        var ghRepos = client.getRepositoriesForUser(username);
        var filteredGHRepos = filterForkedRepositories(ghRepos);
        log.info("Found {} repositories for user: {}", filteredGHRepos.size(), username);
        return fetchRepositoriesDetails(filteredGHRepos);
    }

    private @NonNull List<GitHubRepository> filterForkedRepositories(List<GitHubRepository> ghRepos) {
        return ghRepos
                .stream()
                .filter(repo -> !repo.fork())
                .toList();
    }

    private @NonNull List<Repository> fetchRepositoriesDetails(List<GitHubRepository> ghRepos) {

        var futures = ghRepos.stream()
                .map(this::fetchRepositoryDetailsAsync)
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private CompletableFuture<Repository> fetchRepositoryDetailsAsync(GitHubRepository repo) {
        return CompletableFuture.supplyAsync(() -> fetchRepositoryDetails(repo), EXECUTOR);
    }

    private @NonNull Repository fetchRepositoryDetails(GitHubRepository repo) {
        var branches = fetchBranches(repo);
        return new Repository(
                repo.name(),
                repo.owner().login(),
                branches);
    }

    private @NonNull List<Branch> fetchBranches(GitHubRepository repo) {
        var ghBranches = client.getBranchesForRepo(repo);
        return mapGitHubBranches(ghBranches);
    }

    private @NonNull List<Branch> mapGitHubBranches(List<GitHubBranch> ghBranches) {
        return ghBranches.stream()
                .map(branch -> new Branch(
                        branch.name(),
                        branch.commit().sha()))
                .toList();
    }
}
