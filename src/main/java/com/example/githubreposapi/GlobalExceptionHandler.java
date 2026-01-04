package com.example.githubreposapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleNotFound() {
        var error = new ErrorResponse("user not found", 404);
        return ResponseEntity.status(404).body(error);
    }

    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    public ResponseEntity<?> handleServerError() {
        return ResponseEntity.status(502).body(
                new ErrorResponse("unable to fetch repositories at this time",502)
        );
    }
}
