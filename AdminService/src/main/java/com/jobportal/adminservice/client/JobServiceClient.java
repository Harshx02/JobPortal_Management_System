package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "JOB-SERVICE")
public interface JobServiceClient {

    @GetMapping("/api/jobs")
    Object getAllJobs(@RequestHeader("Authorization") String token);
}
