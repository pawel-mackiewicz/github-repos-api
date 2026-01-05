package com.example.githubreposapi;


import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StopWatch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@WireMockTest(httpPort = 8089)
class IntegrationTests {

    private static final String TEST_USERNAME = "octocat";
    private static final String INVALID_TEST_USERNAME = "invalid_test_username";
    private static final String EMPTY_TEST_USERNAME = "empty_user";

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.url", () -> "http://localhost:8089");
    }

    @Test
    void shouldExcludeForkedRepositories() {
        stubFor(get(urlEqualTo("/users/" + TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "name": "git-consortium",
                                        "fork": false,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    },
                                    {
                                        "name": "boysenberry-repo-1",
                                        "fork": true,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    },
                                    {
                                        "name": "Hello-World",
                                        "fork": false,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USERNAME + "/git-consortium/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "name": "master",
                                        "commit": {
                                            "sha": "abc123def456"
                                        }
                                    },
                                    {
                                        "name": "develop",
                                        "commit": {
                                            "sha": "xyz789uvw012"
                                        }
                                    }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USERNAME + "/Hello-World/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "name": "main",
                                        "commit": {
                                            "sha": "main123commit"
                                        }
                                    }
                                ]
                                """)));

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/repos/{username}",
                String.class,
                TEST_USERNAME);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"repositoryName\":\"git-consortium\"")
                .contains("\"ownerLogin\":\"octocat\"")
                .contains("\"name\":\"master\"")
                .contains("\"lastCommitSha\":\"abc123def456\"")
                .contains("\"name\":\"develop\"")
                .contains("\"lastCommitSha\":\"xyz789uvw012\"")
                .contains("\"repositoryName\":\"Hello-World\"")
                .contains("\"name\":\"main\"")
                .contains("\"lastCommitSha\":\"main123commit\"")
                .doesNotContain("boysenberry-repo-1");

        // Verify that fork repository was NOT requested
        verify(0, getRequestedFor(urlEqualTo("/repos/" + TEST_USERNAME + "/boysenberry-repo-1/branches")));
    }

    @Test
    void shouldIncludeRepositoriesWithoutBranches() {
        // Given
        stubFor(get(urlEqualTo("/users/" + TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "name": "repo-with-branches",
                                        "fork": false,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    },
                                    {
                                        "name": "empty-repo",
                                        "fork": false,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    },
                                    {
                                        "name": "forked-repo",
                                        "fork": true,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USERNAME + "/repo-with-branches/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "name": "main",
                                        "commit": {
                                            "sha": "abc123"
                                        }
                                    }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USERNAME + "/empty-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/repos/{username}",
                String.class,
                TEST_USERNAME);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("\"repositoryName\":\"repo-with-branches\"")
                .contains("\"ownerLogin\":\"octocat\"")
                .contains("\"name\":\"main\"")
                .contains("\"lastCommitSha\":\"abc123\"")
                .contains("\"repositoryName\":\"empty-repo\"");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRepositories() {

        stubFor(get(urlEqualTo("/users/" + EMPTY_TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                ]
                                """)));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/repos/{username}",
                String.class,
                EMPTY_TEST_USERNAME
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    void shouldReturn404WhenUserNotFound() {

        stubFor(get(urlEqualTo("/users/" + INVALID_TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                      "message": "Not Found",
                                      "status": "404"
                                    }
                                ]
                                """)));

        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/repos/{username}",
                ErrorResponse.class,
                INVALID_TEST_USERNAME);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("user not found");
    }

    @Test
    void shouldReturn502WhenUpstreamServiceFails() {
        stubFor(get(urlEqualTo("/users/" + TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                      "message": "Internal Error",
                                      "status": "500"
                                    }
                                ]
                                """)));


        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/repos/{username}",
                ErrorResponse.class,
                TEST_USERNAME
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("unable to fetch from upstream");
        assertThat(response.getBody().status()).isEqualTo(502);
    }

    @Test
    void shouldReturn429WhenUpstreamRateLimitExceeded() {
        // Given
        stubFor(get(urlEqualTo("/users/" + TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(429)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "API rate limit exceeded",
                                  "documentation_url": "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"
                                }
                                """)));

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/repos/{username}",
                ErrorResponse.class,
                TEST_USERNAME
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(429);
        assertThat(response.getBody().message())
                .isEqualTo("rate limit exceeded");
    }

    @Test
    void shouldReturn502WhenUpstreamServiceTimesOut() {
        // Given
        stubFor(get(urlEqualTo("/users/" + TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(408)));

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/repos/{username}",
                ErrorResponse.class,
                TEST_USERNAME
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(502);
        assertThat(response.getBody().message())
                .isEqualTo("upstream service timeout");
    }

    @Test
    void shouldReturn502WhenBranchFetchingFails() {
        // Given
        stubFor(get(urlEqualTo("/users/" + TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {
                                        "name": "test-repo",
                                        "fork": false,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USERNAME + "/test-repo/branches"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "message": "Internal Server Error",
                                  "status": "500"
                                }
                                """)));

        // When
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/repos/{username}",
                ErrorResponse.class,
                TEST_USERNAME
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(502);
        assertThat(response.getBody().message())
                .isEqualTo("unable to fetch from upstream");
    }

    @Test
    void shouldFetchBranchesInParallel() {
        // Given
        stubFor(get(urlEqualTo("/users/" + TEST_USERNAME + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(1000)
                        .withBody("""
                                [
                                    {
                                        "name": "repo-one",
                                        "fork": false,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    },
                                    {
                                        "name": "repo-two",
                                        "fork": false,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    },
                                    {
                                        "name": "forked-repo",
                                        "fork": true,
                                        "owner": {
                                            "login": "octocat"
                                        }
                                    }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USERNAME + "/repo-one/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(1000)
                        .withBody("""
                                [
                                    {
                                        "name": "main",
                                        "commit": {
                                            "sha": "sha123"
                                        }
                                    }
                                ]
                                """)));

        stubFor(get(urlEqualTo("/repos/" + TEST_USERNAME + "/repo-two/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(1000)
                        .withBody("""
                                [
                                    {
                                        "name": "develop",
                                        "commit": {
                                            "sha": "sha456"
                                        }
                                    }
                                ]
                                """)));

        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/repos/{username}",
                String.class,
                TEST_USERNAME
        );

        stopWatch.stop();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify total number of requests (1 for repos + 2 for branches)
        verify(3, getRequestedFor(urlMatching(".*")));

        // Verify time is between 2000ms and 3000ms (parallel execution)
        long totalTime = stopWatch.getTotalTimeMillis();
        assertThat(totalTime).isBetween(2000L, 3000L);
    }
}
