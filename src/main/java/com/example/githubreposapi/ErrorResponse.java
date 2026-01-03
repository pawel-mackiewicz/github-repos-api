package com.example.githubreposapi;

public record ErrorResponse(
        String message,
        Integer status
) {
}
