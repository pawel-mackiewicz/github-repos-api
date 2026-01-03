package com.example.githubreposapi;

public record GitHubRepository(
        String name,
        GitHubOwner owner,
        boolean fork
) {
}
