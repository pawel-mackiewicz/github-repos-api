package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class Controller {

    private final GitHubService gitHubService;

    @GetMapping("/{username}")
    public ResponseEntity<List<Repository>> getEndpoint(@PathVariable String username) {
        List<Repository> res = gitHubService.getUserRepositories(username);
        return ResponseEntity.ok(res);
    }
}
