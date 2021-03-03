package com.tungpv.wallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckAPI {

    @GetMapping("/health_check")
    public ResponseEntity<Boolean> checkHealth() {
        return ResponseEntity.ok().build();
    }
}
