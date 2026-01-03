package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class Controller {

    private final GitHubClient client;

    @GetMapping("/{username}")
    public ResponseEntity<List<Repository>> getEndpoint(@PathVariable String username) {
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

        log.info(new tools.jackson.databind.ObjectMapper().writeValueAsString(res));
        return ResponseEntity.ok(res);
    }


}
