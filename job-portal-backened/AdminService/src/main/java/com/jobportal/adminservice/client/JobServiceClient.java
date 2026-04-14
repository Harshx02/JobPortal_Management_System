package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jobportal.adminservice.dto.response.JobResponse;
import com.jobportal.adminservice.dto.response.PageResponse;

@FeignClient(
    name = "job-service",
    fallbackFactory = JobServiceClientFallback.class
)
public interface JobServiceClient {

    @GetMapping("/api/jobs")
    PageResponse getAllJobs();

    @GetMapping("/api/jobs/{id}")
    JobResponse getJobById(@PathVariable Long id);

    @GetMapping("/api/jobs/count")
    Long getTotalJobs();
}