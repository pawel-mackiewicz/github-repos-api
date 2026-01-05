package com.example.githubreposapi;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GitHubClientException extends RuntimeException {

    private final HttpStatus status;

    public GitHubClientException(HttpStatus httpStatus, String errMessage, Throwable cause) {
        super(errMessage, cause);
        status = httpStatus;
    }
}
