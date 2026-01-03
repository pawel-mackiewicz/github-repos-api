package com.example.githubreposapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/repos")
public class Controller {

    @GetMapping("/{username}")
    public ResponseEntity<String> getEndpoint(@PathVariable String username) {
        return ResponseEntity.ok(username);
    }

}
