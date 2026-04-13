package com.capg.ApplicationService.service;

import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.client.UserClient;
import com.capg.ApplicationService.dto.request.ApplicationRequest;
import com.capg.ApplicationService.dto.response.ApplicationResponse;
import com.capg.ApplicationService.dto.response.JobApplicationResponse;
import com.capg.ApplicationService.dto.response.JobResponse;
import com.capg.ApplicationService.dto.response.UserResponse;
import com.capg.ApplicationService.entity.JobApplication;
import com.capg.ApplicationService.enums.ApplicationStatus;
import com.capg.ApplicationService.exception.ApplicationNotFoundException;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserClient userClient;
    @Mock
    private JobClient jobClient;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private JobApplication application;
    private ApplicationRequest applyReq;
    private JobResponse jobResponse;

    @BeforeEach
    void setUp() {
        applyReq = new ApplicationRequest();
        applyReq.setJobId(101L);

        application = new JobApplication();
        application.setId(1L);
        application.setUserId(1L);
        application.setJobId(101L);
        application.setStatus(ApplicationStatus.PENDING);

        jobResponse = new JobResponse();
        jobResponse.setId(101L);
        jobResponse.setRecruiterId(202L);
        jobResponse.setTitle("Java Developer");
        jobResponse.setCompanyName("Test Corp");
    }

    @Test
    void applyForJob_Success() {
        when(jobClient.getJobById(anyLong())).thenReturn(jobResponse);
        when(applicationRepository.existsByUserIdAndJobId(anyLong(), anyLong())).thenReturn(false);
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(modelMapper.map(any(), any())).thenReturn(new ApplicationResponse());
        when(userClient.getUserById(anyLong(), any())).thenReturn(new UserResponse());

        ApplicationResponse response = applicationService.applyForJob(applyReq, 1L, "JOB_SEEKER", "http://resume");

        assertThat(response).isNotNull();
        verify(applicationRepository).save(any(JobApplication.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any());
    }

    @Test
    void applyForJob_JobNotFound_ThrowsException() {
        when(jobClient.getJobById(anyLong())).thenThrow(new RuntimeException("Job not found with id"));

        assertThatThrownBy(() -> applicationService.applyForJob(applyReq, 1L, "JOB_SEEKER", "url"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Job not found with id");
    }

    @Test
    void applyForJob_EventPublishFailure_Handled() {
        when(jobClient.getJobById(anyLong())).thenReturn(jobResponse);
        when(applicationRepository.existsByUserIdAndJobId(anyLong(), anyLong())).thenReturn(false);
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(modelMapper.map(any(), any())).thenReturn(new ApplicationResponse());
        when(userClient.getUserById(anyLong(), any())).thenThrow(new RuntimeException("User service down"));

        ApplicationResponse response = applicationService.applyForJob(applyReq, 1L, "JOB_SEEKER", "http://resume");

        assertThat(response).isNotNull();
        verify(applicationRepository).save(any(JobApplication.class));
    }

    @Test
    void getUserApplications_UnauthorizedRole_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> applicationService.getUserApplications(1L, "RECRUITER", pageable))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getUserApplications_JobClientFailure_ReturnsFallbackJob() {
        when(applicationRepository.findByUserId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(application)));
        when(modelMapper.map(any(), any())).thenReturn(new ApplicationResponse());
        when(jobClient.getJobById(anyLong())).thenThrow(new RuntimeException("Job service down"));

        Page<ApplicationResponse> result = applicationService.getUserApplications(1L, "JOB_SEEKER", PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getJob().getTitle()).isEqualTo("Job no longer available");
    }

    @Test
    void getJobApplications_UnauthorizedRole_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> applicationService.getJobApplications(101L, "JOB_SEEKER", 202L, pageable))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getJobApplications_WrongRecruiter_ThrowsException() {
        jobResponse.setRecruiterId(999L);
        when(jobClient.getJobById(anyLong())).thenReturn(jobResponse);
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> applicationService.getJobApplications(101L, "RECRUITER", 202L, pageable))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Access Denied! You can view applications for your own jobs");
    }

    @Test
    void getJobApplications_UserClientFailure_Handled() {
        when(jobClient.getJobById(anyLong())).thenReturn(jobResponse);
        when(applicationRepository.findByJobId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(application)));
        when(userClient.getUserById(anyLong(), any())).thenThrow(new RuntimeException("User service down"));

        Page<JobApplicationResponse> result = applicationService.getJobApplications(101L, "RECRUITER", 202L, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getApplicantName()).isEqualTo("N/A");
    }

    @Test
    void updateStatus_UnauthorizedRole_ThrowsException() {
        assertThatThrownBy(() -> applicationService.updateStatus(1L, ApplicationStatus.ACCEPTED, 202L, "JOB_SEEKER"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateStatus_ApplicationNotFound_ThrowsException() {
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.updateStatus(1L, ApplicationStatus.ACCEPTED, 202L, "RECRUITER"))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    void updateStatus_WrongRecruiter_ThrowsException() {
        jobResponse.setRecruiterId(999L);
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(jobClient.getJobById(anyLong())).thenReturn(jobResponse);

        assertThatThrownBy(() -> applicationService.updateStatus(1L, ApplicationStatus.ACCEPTED, 202L, "RECRUITER"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateStatus_EventPublishFailure_Handled() {
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(jobClient.getJobById(anyLong())).thenReturn(jobResponse);
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(application);
        when(modelMapper.map(any(), any())).thenReturn(new ApplicationResponse());
        when(userClient.getUserById(anyLong(), any())).thenThrow(new RuntimeException("Down"));

        ApplicationResponse response = applicationService.updateStatus(1L, ApplicationStatus.ACCEPTED, 202L, "RECRUITER");

        assertThat(response).isNotNull();
        verify(applicationRepository).save(any(JobApplication.class));
    }

    @Test
    void deleteUserApplications() {
        applicationService.deleteUserApplications(1L);
        verify(applicationRepository).deleteByUserId(1L);
    }

    @Test
    void deleteJobApplications() {
        applicationService.deleteJobApplications(101L);
        verify(applicationRepository).deleteByJobId(101L);
    }

    @Test
    void getTotalApplications() {
        when(applicationRepository.count()).thenReturn(10L);
        Long count = applicationService.getTotalApplications();
        assertThat(count).isEqualTo(10L);
    }
}