package com.capg.ApplicationService.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobAppliedEvent {
    private String recruiterEmail;
    private String applicantName;
    private String applicantEmail;
    private String jobTitle;
    private String companyName;
}
