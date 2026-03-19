package com.jobportal.authservice.controller;

import com.jobportal.authservice.dto.request.LoginRequest;
import com.jobportal.authservice.dto.request.RegisterRequest;
import com.jobportal.authservice.dto.response.AuthResponse;
import com.jobportal.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // =====================================================
    // POST /api/auth/register
    // =====================================================
    /**
     * REQUEST BODY:
     * {
     *   "name": "Priya Singh",
     *   "email": "priya@example.com",
     *   "password": "pass1234",
     *   "phone": "9876543210",
     *   "role": "JOB_SEEKER"
     * }
     *
     * RESPONSE (201 Created):
     * {
     *   "token": "eyJhbGci...",
     *   "name": "Priya Singh",
     *   "email": "priya@example.com",
     *   "role": "JOB_SEEKER",
     *   "message": "Registration successful!"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // POST /api/auth/login
    // =====================================================
    /**
     * REQUEST BODY:
     * {
     *   "email": "priya@example.com",
     *   "password": "pass1234"
     * }
     *
     * RESPONSE (200 OK):
     * {
     *   "token": "eyJhbGci...",
     *   "name": "Priya Singh",
     *   "email": "priya@example.com",
     *   "role": "JOB_SEEKER",
     *   "message": "Login successful!"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
