package com.capg.ApplicationService.service;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse apply(ApplicationRequest request);

    List<ApplicationResponse> getUserApplications(Long userId);

    List<ApplicationResponse> getJobApplicants(Long jobId);

    ApplicationResponse updateStatus(StatusUpdateRequest request);
}



