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
    void shouldReturnUserRepositoriesWithoutForks() {

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
    void endpoint_should_return_404_for_invalid_username() {

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
    void should_return_empty_response_if_user_without_repos() {

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
    void should_return_502_when_500_from_gh_server() {
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
                .isEqualTo("unable to fetch repositories at this time");
        assertThat(response.getBody().status()).isEqualTo(502);
    }
}
