package com.jobportal.notificationservice.consumer;

import com.jobportal.notificationservice.dto.*;
import com.jobportal.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailConsumer emailConsumer;

    @Test
    void handleJobPosted_Success() {
        JobPostedEvent event = new JobPostedEvent();
        event.setJobTitle("Java Dev");
        event.setCompanyName("TestCo");
        
        emailConsumer.handleJobPosted(event);
        
        verify(emailService).sendJobPostedEmailToAllJobSeekers(eq("Java Dev"), eq("TestCo"), any(), any(), any());
    }

    @Test
    void handleJobApplied_Success() {
        JobAppliedEvent event = new JobAppliedEvent();
        event.setRecruiterEmail("recruiter@test.com");
        event.setJobTitle("Java Dev");
        
        emailConsumer.handleJobApplied(event);
        
        verify(emailService).sendJobAppliedEmail(eq("recruiter@test.com"), any(), any(), eq("Java Dev"), any());
    }

    @Test
    void handleApplicationStatus_Success() {
        ApplicationStatusEvent event = new ApplicationStatusEvent();
        event.setApplicantEmail("app@test.com");
        event.setStatus("ACCEPTED");
        
        emailConsumer.handleApplicationStatus(event);
        
        verify(emailService).sendApplicationStatusEmail(eq("app@test.com"), any(), any(), any(), eq("ACCEPTED"));
    }

    @Test
    void handleForgotPassword_Success() {
        ForgotPasswordEvent event = new ForgotPasswordEvent();
        event.setEmail("user@test.com");
        event.setOtp("123456");
        
        emailConsumer.handleForgotPassword(event);
        
        verify(emailService).sendOtpEmail("user@test.com", "123456");
    }

    @Test
    void handleApplicationDeleted_Success() {
        UserDeleteEvent event = new UserDeleteEvent();
        event.setUserId(1L);
        emailConsumer.handleApplicationDeleted(event);
        // Method only logs for now, so we verify it doesn't crash
    }
}
