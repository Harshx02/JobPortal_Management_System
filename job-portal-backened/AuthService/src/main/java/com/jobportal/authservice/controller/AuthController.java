package com.jobportal.authservice.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.authservice.dto.request.LoginRequest;
import com.jobportal.authservice.dto.request.RegisterRequest;
import com.jobportal.authservice.dto.request.UpdateProfileRequest;
import com.jobportal.authservice.dto.response.AuthResponse;
import com.jobportal.authservice.dto.response.UserResponse;
import com.jobportal.authservice.service.AuthService;

import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${internal.secret}")
    private String internalSecret;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register API called | email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        log.info("User registered successfully | email: {}", request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login API called | email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        log.info("Login successful | email: {}", request.getEmail());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "/users/{id}/profile-image",
            consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image)
            throws IOException {

        log.info("Upload profile image API called | userId: {} | fileName: {}",
                id, image.getOriginalFilename());

        UserResponse response = authService.uploadProfileImage(id, image);

        log.info("Profile image uploaded successfully | userId: {}", id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Fetch profile API called | userId: {}", userId);

        UserResponse response = authService.getUserById(userId);

        log.info("Profile fetched successfully | userId: {}", userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {

        log.info("Fetch all users API called | role: {} | secret: {}", role, secret != null ? "PRESENT" : "MISSING");

        // ✅ Security check: Allow if ADMIN or valid Internal Secret
        if ((role != null && role.equalsIgnoreCase("ADMIN")) ||
            (secret != null && secret.equals(internalSecret))) {
            
            List<UserResponse> users = authService.getAllUsers();
            return ResponseEntity.ok(users);
        }

        log.warn("Unauthorized access to getAllUsers | role: {}", role);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long loggedInUserId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {

        log.info("Fetch user by ID API called | targetId: {} | loggedInUserId: {} | role: {}", 
                id, loggedInUserId, role);

        // ✅ Security check: Allow if self, ADMIN, or valid Internal Secret
        if ((loggedInUserId != null && loggedInUserId.equals(id)) ||
            (role != null && role.equalsIgnoreCase("ADMIN")) ||
            (secret != null && secret.equals(internalSecret))) {

            UserResponse response = authService.getUserById(id);
            return ResponseEntity.ok(response);
        }

        log.warn("Unauthorized access to getUserById | targetId: {} | loggedInUserId: {}", id, loggedInUserId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PutMapping("/users/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateProfileRequest request) {

        log.info("Update profile API called | userId: {}", userId);

        UserResponse response =
                authService.updateProfile(userId, request);

        return ResponseEntity.ok(response);
    }
}