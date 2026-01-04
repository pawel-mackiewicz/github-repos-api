package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
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
        List<Repository> res = new LinkedList<>();
        for (GitHubRepository ghRepo : ghRepos) {
            var name = ghRepo.name();
            var login = ghRepo.owner().login();

            List<GitHubBranch> ghBranches = client.getBranchesForRepo(ghRepo);
            List<Branch> branches = mapGitHubBranches(ghBranches);

            var repo = new Repository(name, login, branches);
            res.add(repo);
        }
        return res;
    }

    private @NonNull List<Branch> mapGitHubBranches(List<GitHubBranch> ghBranches) {
        List<Branch> branches = new LinkedList<>();
        for (GitHubBranch ghBranch : ghBranches) {
            var branchName = ghBranch.name();
            var sha = ghBranch.commit().sha();
            branches.add(new Branch(branchName,sha));
        }
        return branches;
    }
}
