package com.jobportal.adminservice.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.jobportal.adminservice.client.ApplicationServiceClient;
import com.jobportal.adminservice.client.AuthServiceClient;
import com.jobportal.adminservice.client.JobServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @InjectMocks
    private AdminService adminService;

    private final String MOCK_TOKEN = "Bearer mock-token";
    private final Long USER_ID = 1L;

    @Test
    @DisplayName("Should delete JOB_SEEKER and their applications")
    void testDeleteUserCascading_JobSeeker() {
        // Arrange: Mock Auth Service returning a Job Seeker
        Map<String, Object> mockUser = new HashMap<>();
        mockUser.put("role", "JOB_SEEKER");
        when(authServiceClient.getUserById(USER_ID, MOCK_TOKEN)).thenReturn(mockUser);

        // Act
        adminService.deleteUserCascading(USER_ID, MOCK_TOKEN);

        // Assert: Verify only Application and Auth delete methods are called
        verify(applicationServiceClient, times(1)).deleteApplicationsByUserId(USER_ID, MOCK_TOKEN);
        verify(authServiceClient, times(1)).deleteUser(USER_ID, MOCK_TOKEN);
        
        // Verify Job Service was NOT touched for a Job Seeker
        verifyNoInteractions(jobServiceClient);
    }

    @Test
    @DisplayName("Should delete RECRUITER, their jobs, and associated applications")
    void testDeleteUserCascading_Recruiter() {
        // Arrange: Mock Auth Service returning a Recruiter
        Map<String, Object> mockUser = new HashMap<>();
        mockUser.put("role", "RECRUITER");
        when(authServiceClient.getUserById(USER_ID, MOCK_TOKEN)).thenReturn(mockUser);

        // Mock Job Service returning a list of jobs
        List<Object> mockJobs = new ArrayList<>();
        Map<String, Object> job1 = new HashMap<>();
        job1.put("id", 101);
        mockJobs.add(job1);
        
        when(jobServiceClient.getJobsByRecruiterId(USER_ID, MOCK_TOKEN)).thenReturn(mockJobs);

        // Act
        adminService.deleteUserCascading(USER_ID, MOCK_TOKEN);

        // Assert: Verify cascading chain for Recruiter
        // 1. Check if applications for the job were deleted
        verify(applicationServiceClient, times(1)).deleteApplicationsByJobId(101L, MOCK_TOKEN);
        // 2. Check if the jobs themselves were deleted
        verify(jobServiceClient, times(1)).deleteJobsByRecruiterId(USER_ID, MOCK_TOKEN);
        // 3. Check if the user was deleted
        verify(authServiceClient, times(1)).deleteUser(USER_ID, MOCK_TOKEN);
    }
}