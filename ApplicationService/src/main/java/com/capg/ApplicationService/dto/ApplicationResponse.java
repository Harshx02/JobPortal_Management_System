package com.capg.ApplicationService.dto;

import com.capg.ApplicationService.entity.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private Long userId;
    private Long jobId;
    private String resumeUrl;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}
