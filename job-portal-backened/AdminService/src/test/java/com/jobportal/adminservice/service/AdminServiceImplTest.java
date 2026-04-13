package com.jobportal.adminservice.service;

import com.jobportal.adminservice.client.ApplicationServiceClient;
import com.jobportal.adminservice.client.AuthServiceClient;
import com.jobportal.adminservice.client.JobServiceClient;
import com.jobportal.adminservice.dto.response.JobResponse;
import com.jobportal.adminservice.dto.response.PageResponse;
import com.jobportal.adminservice.dto.response.UserResponse;
import com.jobportal.adminservice.event.UserDeleteEvent;
import com.jobportal.adminservice.producer.UserDeleteProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private JobServiceClient jobServiceClient;
    @Mock
    private ApplicationServiceClient applicationServiceClient;
    @Mock
    private UserDeleteProducer userDeleteProducer;

    @InjectMocks
    private AdminServiceImpl adminService;

    private UserResponse userResponse;
    private JobResponse jobResponse;
    private PageResponse pageResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@test.com");
        userResponse.setRole("JOB_SEEKER");

        jobResponse = new JobResponse();
        jobResponse.setId(101L);
        jobResponse.setTitle("Java Dev");

        pageResponse = new PageResponse();
        pageResponse.setTotalElements(10L);
    }

    @Test
    void getAllUsers_Success() {
        when(authServiceClient.getAllUsers(any())).thenReturn(List.of(userResponse));
        List<UserResponse> result = adminService.getAllUsers();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getUserById_Success() {
        when(authServiceClient.getUserById(anyLong(), any())).thenReturn(userResponse);
        UserResponse result = adminService.getUserById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void deleteUser_StartsSaga() {
        when(authServiceClient.getUserById(anyLong(), any())).thenReturn(userResponse);
        adminService.deleteUser(1L);
        verify(userDeleteProducer).startSaga(any(UserDeleteEvent.class));
    }

    @Test
    void getAllJobs_Success() {
        when(jobServiceClient.getAllJobs()).thenReturn(pageResponse);
        PageResponse result = adminService.getAllJobs();
        assertThat(result.getTotalElements()).isEqualTo(10L);
    }

    @Test
    void getJobById_Success() {
        when(jobServiceClient.getJobById(anyLong())).thenReturn(jobResponse);
        JobResponse result = adminService.getJobById(101L);
        assertThat(result.getId()).isEqualTo(101L);
    }

    @Test
    void getReports_Success() {
        when(authServiceClient.getAllUsers(any())).thenReturn(List.of(userResponse));
        when(jobServiceClient.getAllJobs()).thenReturn(pageResponse);
        when(applicationServiceClient.getTotalApplications()).thenReturn(5L);

        Map<String, Object> reports = adminService.getReports();

        assertThat(reports.get("totalUsers")).isEqualTo(1L);
        assertThat(reports.get("totalJobs")).isEqualTo(10L);
        assertThat(reports.get("totalApplications")).isEqualTo(5L);
    }

    @Test
    void fallbackGetAllUsers_ReturnsEmptyList() {
        List<UserResponse> result = adminService.fallbackGetAllUsers(new RuntimeException("Error"));
        assertThat(result).isEmpty();
    }
}