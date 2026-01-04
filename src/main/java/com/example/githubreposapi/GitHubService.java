package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient client;

    public @NonNull List<Repository> getUserRepositories(String username) {
        var ghRepos = client.getRepositoriesForUser(username);
        var filteredGHRepos = filterForkedRepositories(ghRepos);
        return fetchBranchesAndBuildRepositories(filteredGHRepos);
    }

    private @NonNull List<GitHubRepository> filterForkedRepositories(List<GitHubRepository> ghRepos) {
        return ghRepos
                .stream()
                .filter(repo -> !repo.fork())
                .toList();
    }

    private @NonNull List<Repository> fetchBranchesAndBuildRepositories(List<GitHubRepository> ghRepos) {
        return ghRepos.stream()
                .map(repo -> {
                    var ghBranches = client.getBranchesForRepo(repo);
                    var branches = mapGitHubBranches(ghBranches);
                    return new Repository(
                            repo.name(),
                            repo.owner().login(),
                            branches);
                })
                .toList();
    }

    private @NonNull List<Branch> mapGitHubBranches(List<GitHubBranch> ghBranches) {
        return ghBranches.stream()
                .map(branch -> new Branch(
                        branch.name(),
                        branch.commit().sha()))
                .toList();
    }
}
