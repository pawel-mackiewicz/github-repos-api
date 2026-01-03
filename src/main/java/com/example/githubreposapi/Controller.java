package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;


@Slf4j
@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class Controller {

    private final GitHubClient client;

    @GetMapping("/{username}")
    public ResponseEntity<String> getEndpoint(@PathVariable String username) {
        try {
            client.getRepositoriesForUser(username);
            return ResponseEntity.ok(username);
        } catch (HttpClientErrorException e) {
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


}
