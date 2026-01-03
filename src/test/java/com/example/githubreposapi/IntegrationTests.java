package com.example.githubreposapi;


import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@WireMockTest(httpPort = 8089)
class IntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    void endpoint_should_return_provided_username() {
        var testUsername = "test_username";
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/repos/{username}",
                String.class,
                testUsername);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testUsername);
    }
}
