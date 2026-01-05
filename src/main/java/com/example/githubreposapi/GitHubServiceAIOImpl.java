package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class GitHubServiceAIOImpl implements GitHubService {

    private final static ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final GitHubClient client;

    @Override
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
