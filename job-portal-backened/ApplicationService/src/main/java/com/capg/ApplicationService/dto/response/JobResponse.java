package com.capg.ApplicationService.dto.response;

import lombok.Data;

@Data
public class JobResponse {
    private Long id;
    private String title;
    private String companyName;
    private Double salary;
    private String location;
    private Long recruiterId;
}