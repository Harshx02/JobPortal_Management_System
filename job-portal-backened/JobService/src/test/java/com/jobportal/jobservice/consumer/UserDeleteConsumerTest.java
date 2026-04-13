package com.jobportal.jobservice.consumer;

import com.jobportal.jobservice.config.RabbitMQConfig;
import com.jobportal.jobservice.event.UserDeleteEvent;
import com.jobportal.jobservice.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeleteConsumerTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserDeleteConsumer userDeleteConsumer;

    private UserDeleteEvent recruiterEvent;
    private UserDeleteEvent seekerEvent;

    @BeforeEach
    void setUp() {
        recruiterEvent = new UserDeleteEvent(1L, "RECRUITER", "PENDING", null);
        seekerEvent = new UserDeleteEvent(2L, "JOB_SEEKER", "PENDING", null);
    }

    @Test
    void handle_RecruiterWithJobs_DeletesJobsAndPublishes() {
        // Arrange
        when(jobRepository.existsByRecruiterId(1L)).thenReturn(true);

        // Act
        userDeleteConsumer.handle(recruiterEvent);

        // Assert
        verify(jobRepository, times(1)).deleteByRecruiterId(1L);
        verify(rabbitTemplate, times(1)).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.RK_JOBS_DELETED), any(UserDeleteEvent.class));
    }

    @Test
    void handle_RecruiterNoJobs_DoesNotDeleteButPublishes() {
        // Arrange
        when(jobRepository.existsByRecruiterId(1L)).thenReturn(false);

        // Act
        userDeleteConsumer.handle(recruiterEvent);

        // Assert
        verify(jobRepository, never()).deleteByRecruiterId(anyLong());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(UserDeleteEvent.class));
    }

    @Test
    void handle_JobSeeker_PublishesWithoutDeleting() {
        // Act
        userDeleteConsumer.handle(seekerEvent);

        // Assert
        verify(jobRepository, never()).existsByRecruiterId(anyLong());
        verify(jobRepository, never()).deleteByRecruiterId(anyLong());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(UserDeleteEvent.class));
    }
}
