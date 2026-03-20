package com.capg.ApplicationService.dto;

import com.capg.ApplicationService.entity.ApplicationStatus;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    private Long applicationId;
    private ApplicationStatus status;
}
