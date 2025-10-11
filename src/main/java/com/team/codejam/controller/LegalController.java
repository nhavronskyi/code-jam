package com.team.codejam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegalController {
    @GetMapping("/api/legal/terms")
    public ResponseEntity<String> getTerms() {
        // In production, serve from static file or DB
        return ResponseEntity.ok("Terms of Service: ...");
    }

    @GetMapping("/api/legal/privacy")
    public ResponseEntity<String> getPrivacy() {
        // In production, serve from static file or DB
        return ResponseEntity.ok("Privacy Policy: ...");
    }
}

