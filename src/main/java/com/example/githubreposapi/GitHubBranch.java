package com.example.githubreposapi;

public record GitHubBranch(
        String name,
        GitHubCommit commit
){
}
