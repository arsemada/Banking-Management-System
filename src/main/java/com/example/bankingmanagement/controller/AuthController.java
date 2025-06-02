package com.example.bankingmanagement.controller;

import com.example.bankingmanagement.payload.request.LoginRequest;
import com.example.bankingmanagement.payload.request.SignupRequest;
import com.example.bankingmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin; // Make sure this is present for CORS

@CrossOrigin(origins = "*", maxAge = 3600) // Ensure CORS is enabled for frontend communication
@RestController
@RequestMapping("/api/auth") // Changed to /api/auth as per previous discussion and frontend update
public class AuthController {

    @Autowired
    AuthService authService; // This service should encapsulate the Spring Security authentication logic

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return authService.registerUser(signUpRequest);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // The authService.authenticateUser method should return ResponseEntity<JwtResponse>
        // as structured in previous discussions, which includes the token, username, and roles.
        return authService.authenticateUser(loginRequest);
    }
}