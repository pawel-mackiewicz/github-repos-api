package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class Controller {

    private final GitHubClient client;

    @GetMapping("/{username}")
    public ResponseEntity<String> getEndpoint(@PathVariable String username) {
        client.getRepositoriesForUser(username);
        return ResponseEntity.ok(username);
    }


}
