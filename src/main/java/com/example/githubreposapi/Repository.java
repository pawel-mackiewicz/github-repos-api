package com.example.githubreposapi;

import java.util.List;

public record Repository(
        String repositoryName,
        String ownerLogin,
        List<Branch> branches
) {
}
