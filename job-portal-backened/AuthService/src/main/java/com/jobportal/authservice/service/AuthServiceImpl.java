package com.jobportal.authservice.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.authservice.dto.request.LoginRequest;
import com.jobportal.authservice.dto.request.RegisterRequest;
import com.jobportal.authservice.dto.request.UpdateProfileRequest;
import com.jobportal.authservice.dto.response.AuthResponse;
import com.jobportal.authservice.dto.response.UserResponse;
import com.jobportal.authservice.entity.User;
import com.jobportal.authservice.enums.UserRole;
import com.jobportal.authservice.exception.DuplicateEmailException;
import com.jobportal.authservice.exception.InvalidCredentialsException;
import com.jobportal.authservice.exception.UnauthorizedException;
import com.jobportal.authservice.exception.UserNotFoundException;
import com.jobportal.authservice.repository.UserRepository;
import com.jobportal.authservice.security.JwtUtil;
import com.jobportal.authservice.dto.OtpDetails;
import com.jobportal.authservice.event.ForgotPasswordEvent;
import com.jobportal.authservice.config.RabbitMQConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.Random;
import java.util.concurrent.TimeUnit;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final long OTP_EXPIRY_MINUTES = 5;


    // REGISTER
    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail();
        if (email != null) email = email.trim().toLowerCase();

        log.info("Register service called | email: {} | role: {}",
                email, request.getRole());

        if (request.getRole() == UserRole.ADMIN) {
            log.warn("Attempt to register ADMIN user | email: {}", email);
            throw new UnauthorizedException("Admin registration is not allowed!");
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Duplicate email registration attempt | email: {}", email);
            throw new DuplicateEmailException(
                    "Email already registered: " + email);
        }

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        log.info("User saved successfully | userId: {} | email: {}",
                savedUser.getId(), savedUser.getEmail());

        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name()
        );

        log.debug("JWT generated for new user | userId: {}", savedUser.getId());

        return new AuthResponse(
                token,
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                "Registration successful!"
        );
    }

    // LOGIN
    @Override
    public AuthResponse login(LoginRequest request) {
        final String email = (request.getEmail() != null) ? request.getEmail().trim().toLowerCase() : null;

        log.info("Login service called | email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found during login | email: {}", email);
                    return new UserNotFoundException(
                            "User not found with email: " + email);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid login attempt | email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password!");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        log.info("Login successful | userId: {}", user.getId());
        log.debug("JWT generated for user | userId: {}", user.getId());

        return new AuthResponse(
                token,
                user.getName(),
                user.getEmail(),
                user.getRole(),
                "Login successful!"
        );
    }

    // UPLOAD PROFILE IMAGE
    @Override
    public UserResponse uploadProfileImage(Long userId,
                                           MultipartFile file) throws IOException {

        log.info("Uploading profile image | userId: {} | fileName: {}",
                userId, file.getOriginalFilename());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for image upload | userId: {}", userId);
                    return new UserNotFoundException(
                            "User not found with id: " + userId);
                });

        String imageUrl = cloudinaryService.uploadProfileImage(file);

        user.setProfileImageUrl(imageUrl);
        User updated = userRepository.save(user);

        log.info("Profile image updated successfully | userId: {}", userId);

        return modelMapper.map(updated, UserResponse.class);
    }

    // UPDATE PROFILE
    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {

        log.info("Update profile service called | userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for profile update | userId: {}", userId);
                    return new UserNotFoundException(
                            "User not found with id: " + userId);
                });

        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getSkills() != null) user.setSkills(request.getSkills());

        User updatedUser = userRepository.save(user);

        log.info("Profile updated successfully | userId: {}", userId);

        return modelMapper.map(updatedUser, UserResponse.class);
    }

    // GET ALL USERS
    @Override
    public List<UserResponse> getAllUsers() {

        log.info("Fetching all users");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserResponse.class))
                .collect(Collectors.toList());

        log.debug("Total users fetched: {}", users.size());

        return users;
    }

    // GET USER BY ID
    @Override
    public UserResponse getUserById(Long id) {

        log.info("Fetching user by ID | userId: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found | userId: {}", id);
                    return new UserNotFoundException(
                            "User not found with id: " + id);
                });

        return modelMapper.map(user, UserResponse.class);
    }

    // DELETE USER
    @Override
    public void deleteUser(Long id) {

        log.info("Deleting user | userId: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found for deletion | userId: {}", id);
                    return new UserNotFoundException(
                            "User not found with id: " + id);
                });

        userRepository.delete(user);

        log.info("User deleted successfully | userId: {}", id);
    }

    @Override
    public void forgotPassword(String email) {
        if (email != null) email = email.trim().toLowerCase();
        log.info("Requesting OTP for forgot password | email: {}", email);
        
        final String searchEmail = email;
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + searchEmail));

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        // Store in Redis
        OtpDetails otpDetails = OtpDetails.builder()
                .otp(otp)
                .verified(false)
                .build();
        
        redisTemplate.opsForValue().set(OTP_KEY_PREFIX + email, otpDetails, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        
        // Send to RabbitMQ
        ForgotPasswordEvent event = new ForgotPasswordEvent(email, otp);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_FORGOT_PASSWORD, event);
        
        log.info("OTP generated and event published | email: {}", email);
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        log.info("Verifying OTP | email: {}", email);
        
        OtpDetails details = (OtpDetails) redisTemplate.opsForValue().get(OTP_KEY_PREFIX + email);
        
        if (details != null && details.getOtp().equals(otp)) {
            details.setVerified(true);
            // Update in redis to mark as verified for password reset step
            redisTemplate.opsForValue().set(OTP_KEY_PREFIX + email, details, 10, TimeUnit.MINUTES);
            log.info("OTP verified successfully | email: {}", email);
            return true;
        }
        
        log.warn("Invalid OTP attempt | email: {}", email);
        return false;
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        final String normalizedEmail = (email != null) ? email.trim().toLowerCase() : null;
        log.info("Resetting password | email: {}", normalizedEmail);
        
        OtpDetails details = (OtpDetails) redisTemplate.opsForValue().get(OTP_KEY_PREFIX + normalizedEmail);
        
        if (details == null || !details.isVerified()) {
            log.error("Password reset attempt without OTP verification | email: {}", normalizedEmail);
            throw new UnauthorizedException("OTP not verified for this email!");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + normalizedEmail));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Delete OTP from redis after successful reset
        redisTemplate.delete(OTP_KEY_PREFIX + email);
        
        log.info("Password reset successfully | email: {}", email);
    }

    @Override
    public Long countUsersByRole(String role) {
        log.info("Counting users by role | role: {}", role);
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            return userRepository.countByRole(userRole);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role provided for count | role: {}", role);
            return 0L;
        }
    }
}

