package com.jobportal.jobservice.dto;

import lombok.Data;

@Data
public class JobFilterDto {
    private String title;
    private String skill;
    private String location;
    private String companyName;
    private Double minSalary;
    private Double maxSalary;
    private Integer minExperience;
    private Integer maxExperience;
}
