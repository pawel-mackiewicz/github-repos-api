package com.example.githubreposapi;

import java.util.List;

public record Repository(
        String name,
        String ownerLogin,
        List<Branch> branches
) {
}
