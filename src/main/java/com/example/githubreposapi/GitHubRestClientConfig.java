package com.example.githubreposapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GitHubRestClientConfig {

    public static final String GITHUB_ACCEPT_HEADER = "application/vnd.github+json";

    @Bean
    public RestClient githubRestClient(
            RestClient.Builder builder,
            @Value("${github.api.url:https://api.github.com}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", GITHUB_ACCEPT_HEADER)
                .build();
    }
}
