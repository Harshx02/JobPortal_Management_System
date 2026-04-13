package com.jobportal.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.authservice.dto.request.*;
import com.jobportal.authservice.dto.response.AuthResponse;
import com.jobportal.authservice.dto.response.UserResponse;
import com.jobportal.authservice.enums.UserRole;
import com.jobportal.authservice.security.JwtUtil;
import com.jobportal.authservice.security.UserDetailsServiceImpl;
import com.jobportal.authservice.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Prateek");
        request.setEmail("test@gmail.com");
        request.setPassword("123456");
        request.setRole(UserRole.JOB_SEEKER);

        AuthResponse response = new AuthResponse("token", "Prateek", "test@gmail.com", UserRole.JOB_SEEKER, "Registration successful!");
        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("123456");

        AuthResponse response = new AuthResponse("token", "Prateek", "test@gmail.com", UserRole.JOB_SEEKER, "Login successful!");
        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void testGetProfile() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        when(authService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/auth/profile")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        when(authService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/auth/users").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@gmail.com"));
    }

    @Test
    void testGetUserById() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        when(authService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/auth/users/1").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void testUpdateProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Updated");

        UserResponse response = new UserResponse();
        response.setName("Updated");

        when(authService.updateProfile(1L, request)).thenReturn(response);

        mockMvc.perform(put("/api/auth/users/profile")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void testDeleteUser() throws Exception {
        doNothing().when(authService).deleteUser(1L);

        mockMvc.perform(delete("/api/auth/users/1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully!"));
    }

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@gmail.com");

        doNothing().when(authService).forgotPassword("test@gmail.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP sent to your email!"));
    }

    @Test
    void testVerifyOtp() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@gmail.com");
        request.setOtp("123456");

        when(authService.verifyOtp("test@gmail.com", "123456")).thenReturn(true);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("OTP Verified!"));
    }

    @Test
    void testResetPassword() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@gmail.com");
        request.setNewPassword("newpass");

        doNothing().when(authService).resetPassword("test@gmail.com", "newpass");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successful!"));
    }

    @Test
    void testUploadProfileImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());
        UserResponse response = new UserResponse();
        response.setId(1L);

        when(authService.uploadProfileImage(1L, file)).thenReturn(response);

        mockMvc.perform(multipart("/api/auth/users/1/profile-image").file(file))
                .andExpect(status().isOk());
    }
}