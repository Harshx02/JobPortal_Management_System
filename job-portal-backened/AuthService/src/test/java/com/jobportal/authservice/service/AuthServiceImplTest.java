package com.jobportal.authservice.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Priya Singh");
        registerRequest.setEmail("priya@gmail.com");
        registerRequest.setPassword("priya123");
        registerRequest.setPhone("9876543210");
        registerRequest.setRole(UserRole.JOB_SEEKER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("priya@gmail.com");
        loginRequest.setPassword("priya123");

        user = new User();
        user.setId(1L);
        user.setName("Priya Singh");
        user.setEmail("priya@gmail.com");
        user.setPassword("hashedPassword");
        user.setRole(UserRole.JOB_SEEKER);
        user.setPhone("9876543210");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("mockToken");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mockToken");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void register_AsAdmin_ThrowsException() {
        registerRequest.setRole(UserRole.ADMIN);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString())).thenReturn("mockToken");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mockToken");
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void uploadProfileImage_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(cloudinaryService.uploadProfileImage(any())).thenReturn("http://image.url");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(), any())).thenReturn(new UserResponse());

        authService.uploadProfileImage(1L, file);

        verify(cloudinaryService).uploadProfileImage(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_Success() {
        UpdateProfileRequest updateReq = new UpdateProfileRequest();
        updateReq.setName("New Name");
        updateReq.setPhone("1122334455");
        updateReq.setBio("Bio");
        updateReq.setLocation("Location");
        updateReq.setSkills("Java, Spring Boot");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(), any())).thenReturn(new UserResponse());

        authService.updateProfile(1L, updateReq);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        authService.getAllUsers();
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        authService.getUserById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        authService.deleteUser(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void forgotPassword_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.forgotPassword("priya@gmail.com");

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(ForgotPasswordEvent.class));
    }

    @Test
    void verifyOtp_Success() {
        OtpDetails details = new OtpDetails("123456", false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(details);

        boolean result = authService.verifyOtp("priya@gmail.com", "123456");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOtp_Failure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        boolean result = authService.verifyOtp("priya@gmail.com", "123456");

        assertThat(result).isFalse();
    }

    @Test
    void resetPassword_Success() {
        OtpDetails details = new OtpDetails("123456", true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(details);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");

        authService.resetPassword("priya@gmail.com", "newPassword");

        verify(userRepository).save(any(User.class));
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void resetPassword_Unverified_ThrowsException() {
        OtpDetails details = new OtpDetails("123456", false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(details);

        assertThatThrownBy(() -> authService.resetPassword("priya@gmail.com", "newPassword"))
                .isInstanceOf(UnauthorizedException.class);
    }
}
