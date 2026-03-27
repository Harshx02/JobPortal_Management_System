package com.capg.ApplicationService.service;

import java.util.List;

import com.capg.ApplicationService.dto.request.ApplicationRequest;
import com.capg.ApplicationService.dto.response.ApplicationResponse;
import com.capg.ApplicationService.dto.response.JobApplicationResponse;
import com.capg.ApplicationService.enums.ApplicationStatus;

public interface ApplicationService {

    ApplicationResponse applyForJob(ApplicationRequest request, Long userId, String role, String resumeUrl);

    List<ApplicationResponse> getUserApplications(Long userId, String role);

    List<JobApplicationResponse> getJobApplications(Long jobId, String role, Long recruiterId);

    ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status, Long recruiterId, String role);

    void deleteUserApplications(Long userId);

    void deleteJobApplications(Long jobId);

    Long getTotalApplications();
}
