package com.capg.ApplicationService.dto.response;

import java.time.LocalDateTime;

import com.capg.ApplicationService.enums.ApplicationStatus;

import lombok.Data;

@Data
public class JobApplicationResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private String resumeUrl;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    // User details
    private String applicantName;
    private String applicantEmail;
}