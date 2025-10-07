package com.nuvesta.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, String>> authFallback() {
        return buildResponse("Authentication service is currently unavailable. Please try again shortly.");
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, String>> userFallback() {
        return buildResponse("User service is currently unavailable. Please try again shortly.");
    }

    @GetMapping("/market-data")
    public ResponseEntity<Map<String, String>> marketDataFallback() {
        return buildResponse("Market data service is currently unavailable. Please try again shortly.");
    }

    private ResponseEntity<Map<String, String>> buildResponse(String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", message));
    }
}