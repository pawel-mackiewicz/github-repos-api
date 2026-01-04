package com.example.githubreposapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubClientException.class)
    public ResponseEntity<ErrorResponse> handleGitHubClientExceptions( GitHubClientException ex) {
        var res = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(res);
    }
}
