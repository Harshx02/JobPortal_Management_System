package com.jobportal.notificationservice.service;

import com.jobportal.notificationservice.client.UserClient;
import com.jobportal.notificationservice.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "internalSecret", "testSecret");
    }

    @Test
    void sendJobPostedEmailToAllJobSeekers_Success() {
        UserResponse seeker = new UserResponse();
        seeker.setEmail("seeker@test.com");
        seeker.setRole("JOB_SEEKER");
        seeker.setName("Seeker");

        when(userClient.getAllUsers(anyString())).thenReturn(List.of(seeker));

        emailService.sendJobPostedEmailToAllJobSeekers("Java Dev", "TestCo", "Remote", 50000.0, 3);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendJobPostedEmailToAllJobSeekers_HandlesError() {
        when(userClient.getAllUsers(anyString())).thenThrow(new RuntimeException("API Error"));
        emailService.sendJobPostedEmailToAllJobSeekers("Java Dev", "TestCo", "Remote", 50000.0, 3);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendJobAppliedEmail_Success() {
        emailService.sendJobAppliedEmail("recruiter@test.com", "Applicant", "app@test.com", "Java Dev", "TestCo");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendApplicationStatusEmail_AllStatuses() {
        String[] statuses = {"UNDER_REVIEW", "SHORTLISTED", "ACCEPTED", "REJECTED", "UNKNOWN"};
        for (String status : statuses) {
            emailService.sendApplicationStatusEmail("app@test.com", "Applicant", "Java Dev", "TestCo", status);
        }
        verify(mailSender, times(5)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_Success() {
        emailService.sendOtpEmail("user@test.com", "123456");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
