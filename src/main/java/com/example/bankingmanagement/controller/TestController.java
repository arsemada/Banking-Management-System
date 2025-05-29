package com.example.bankingmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello-authenticated")
    public ResponseEntity<String> helloAuthenticatedUser() {
        return ResponseEntity.ok("Hello! You are an authenticated user.");
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')") // This requires the user to have the 'ADMIN' role
    public ResponseEntity<String> adminOnlyEndpoint() {
        return ResponseEntity.ok("Welcome, Admin! Only admins can see this.");
    }
}