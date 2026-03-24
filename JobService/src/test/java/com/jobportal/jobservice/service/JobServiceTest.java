package com.jobportal.jobservice.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jobportal.jobservice.dto.JobRequestDto;
import com.jobportal.jobservice.dto.JobResponseDto;
import com.jobportal.jobservice.entity.Job;
import com.jobportal.jobservice.exceptions.JobNotFoundException;
import com.jobportal.jobservice.exceptions.UnauthorizedException;
import com.jobportal.jobservice.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private Job mockJob;
    private JobRequestDto jobRequest;
    private JobResponseDto jobResponse;

    @BeforeEach
    void setUp() {
        // Initialize Request DTO
        jobRequest = new JobRequestDto();
        jobRequest.setTitle("Senior Java Developer");
        jobRequest.setCompanyName("Tech Corp");
        jobRequest.setLocation("Remote");
        jobRequest.setSalary(120000.0);
        jobRequest.setExperience(5);
        jobRequest.setDescription("Leading Java projects");

        // Initialize Entity
        mockJob = new Job();
        mockJob.setId(1L);
        mockJob.setTitle("Java Developer");
        mockJob.setRecruiterId(100L);

        // Initialize Response DTO
        jobResponse = new JobResponseDto();
        jobResponse.setId(1L);
        jobResponse.setTitle("Senior Java Developer");
    }

    @Test
    @DisplayName("Should update job successfully when recruiter owns the job")
    void testUpdateJob_Success() {
        // 1. Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.of(mockJob));
        
        // This handles the in-place mapping: modelMapper.map(dto, job)
        // We use doNothing() because this specific ModelMapper method returns void
        doNothing().when(modelMapper).map(any(JobRequestDto.class), any(Job.class));
        
        // Handle the final save and map back to Response DTO
        when(jobRepository.save(any(Job.class))).thenReturn(mockJob);
        when(modelMapper.map(any(Job.class), eq(JobResponseDto.class))).thenReturn(jobResponse);

        // 2. Act
        JobResponseDto result = jobService.updateJob(1L, jobRequest, 100L);

        // 3. Assert
        assertNotNull(result);
        assertEquals("Senior Java Developer", result.getTitle());
        
        // Verify interactions
        verify(jobRepository).findById(1L);
        verify(modelMapper).map(eq(jobRequest), eq(mockJob)); // Verify the in-place update
        verify(jobRepository).save(mockJob);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException during update if recruiter ID mismatch")
    void testUpdateJob_Unauthorized() {
        // Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.of(mockJob));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            jobService.updateJob(1L, jobRequest, 999L); // Wrong Recruiter ID
        });

        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Should throw JobNotFoundException when updating non-existent job")
    void testUpdateJob_NotFound() {
        // Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(JobNotFoundException.class, () -> {
            jobService.updateJob(1L, jobRequest, 100L);
        });
    }
}