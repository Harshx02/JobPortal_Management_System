package com.jobportal.authservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jobportal.authservice.dto.request.LoginRequest;
import com.jobportal.authservice.dto.request.RegisterRequest;
import com.jobportal.authservice.dto.response.AuthResponse;
import com.jobportal.authservice.dto.response.UserResponse;
import com.jobportal.authservice.entity.User;
import com.jobportal.authservice.exception.DuplicateEmailException;
import com.jobportal.authservice.exception.InvalidCredentialsException;
import com.jobportal.authservice.exception.UserNotFoundException;
import com.jobportal.authservice.repository.UserRepository;
import com.jobportal.authservice.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // =====================================================
    // REGISTER
    // =====================================================
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    "Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        // UPDATED: pass role and userId into token
        // API Gateway will extract these and forward as headers
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.getId()
        );

        return new AuthResponse(
                token,
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                "Registration successful!"
        );
    }

    // =====================================================
    // LOGIN
    // =====================================================
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password!");
        }

        // UPDATED: pass role and userId into token
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole(),
                "Login successful!"
        );
    }

    // =====================================================
    // GET ALL USERS - used by Admin Service
    // =====================================================
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserResponse response = new UserResponse();
                    response.setId(user.getId());
                    response.setName(user.getName());
                    response.setEmail(user.getEmail());
                    response.setPhone(user.getPhone());
                    response.setRole(user.getRole());
                    response.setCreatedAt(user.getCreatedAt());
                    return response;
                })
                .collect(Collectors.toList());
    }

    // GET USER BY ID
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id));
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // DELETE USER
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}