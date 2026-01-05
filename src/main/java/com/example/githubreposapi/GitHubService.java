package com.example.githubreposapi;

import org.jspecify.annotations.NonNull;

import java.util.List;

public interface GitHubService {
    @NonNull List<Repository> getUserRepositories(String username);
}
