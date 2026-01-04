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
        assert ghRepos != null;
        var filteredGHRepos = ghRepos
                .stream()
                .filter(repo -> !repo.fork())
                .toList();

        List<Repository> res = new LinkedList<>();
        for (GitHubRepository ghRepo : filteredGHRepos) {
            var name = ghRepo.name();
            var login = ghRepo.owner().login();
            List<GitHubBranch> ghBranches = client.getBranchesForRepo(ghRepo);
            List<Branch> branches = new LinkedList<>();
            for (GitHubBranch ghBranch : ghBranches) {
                var branchName = ghBranch.name();
                var sha = ghBranch.commit().sha();
                branches.add(new Branch(branchName,sha));
            }
            var repo = new Repository(name, login, branches);
            res.add(repo);
        }
        return res;
    }
}
