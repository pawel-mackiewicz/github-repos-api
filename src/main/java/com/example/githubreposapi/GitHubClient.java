package com.example.githubreposapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubClient {

    private static final String ERROR_USER_NOT_FOUND = "user not found";
    private static final String ERROR_RATE_LIMIT = "rate limit exceeded";
    private static final String ERROR_SERVER_TIMEOUT = "upstream service timeout";
    private static final String ERROR_UNABLE_TO_FETCH = "unable to fetch from upstream";

    private final RestClient gitHubClient;

    public List<GitHubRepository> getRepositoriesForUser(String username) {
        return gitHubClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (_, response) -> {
                    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new GitHubClientException(HttpStatus.NOT_FOUND, ERROR_USER_NOT_FOUND);
                    }
                    handleResponseError(response);
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<GitHubBranch> getBranchesForRepo(GitHubRepository repo) {
        return gitHubClient.get()
                .uri("/repos/{owner}/{repo}/branches", repo.owner().login(), repo.name())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (_, response) ->
                        handleResponseError(response))
                .body(new ParameterizedTypeReference<>() {});
    }

    private void handleResponseError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new GitHubClientException(HttpStatus.TOO_MANY_REQUESTS, ERROR_RATE_LIMIT);
        }
        if (response.getStatusCode() == HttpStatus.REQUEST_TIMEOUT) {
            throw new GitHubClientException(HttpStatus.BAD_GATEWAY,ERROR_SERVER_TIMEOUT);
        }
        //fallback
        throw new GitHubClientException(HttpStatus.BAD_GATEWAY, ERROR_UNABLE_TO_FETCH);
    }
}
