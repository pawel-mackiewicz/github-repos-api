package com.example.githubreposapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubClientException.class)
    public ResponseEntity<ErrorResponse> handleGitHubClientExceptions( GitHubClientException ex) {
        var res = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(res);
    }

    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    public ResponseEntity<?> handleServerError() {
        return ResponseEntity.status(502).body(
                new ErrorResponse("unable to fetch repositories at this time",502)
        );
    }
}
