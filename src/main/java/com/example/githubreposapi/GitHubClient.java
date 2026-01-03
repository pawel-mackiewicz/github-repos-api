package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GitHubClient {

    private final RestClient gitHubClient;

    public @Nullable String getRepositoriesForUser(String username) {
        return gitHubClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .body(String.class);
    }
}
