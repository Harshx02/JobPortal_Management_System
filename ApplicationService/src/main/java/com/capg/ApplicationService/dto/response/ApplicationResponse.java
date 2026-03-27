package com.capg.ApplicationService.dto.response;

import java.time.LocalDateTime;

import com.capg.ApplicationService.enums.ApplicationStatus;

import lombok.Data;

@Data
public class ApplicationResponse {
	private Long id;
	private Long userId;
	private String resumeUrl;
	private ApplicationStatus status;
	private LocalDateTime appliedAt;
	
	// Job details as object
    private JobResponse job;
}
