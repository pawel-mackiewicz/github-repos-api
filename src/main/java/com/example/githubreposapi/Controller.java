package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class Controller {

    private final GitHubClient client;

    @GetMapping("/{username}")
    public ResponseEntity<List<GitHubRepository>> getEndpoint(@PathVariable String username) {
        var ghRepos = client.getRepositoriesForUser(username);
        assert ghRepos != null;
        var filteredGHRepos = ghRepos
                .stream()
                .filter(repo -> !repo.fork())
                .toList();
        return ResponseEntity.ok(filteredGHRepos);
    }


}
