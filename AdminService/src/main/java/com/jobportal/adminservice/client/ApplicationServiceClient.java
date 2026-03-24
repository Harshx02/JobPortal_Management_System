package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "APPLICATION-SERVICE")
public interface ApplicationServiceClient {

    @DeleteMapping("/api/applications/user/{userId}")
    void deleteApplicationsByUserId(@PathVariable("userId") Long userId, @RequestHeader("Authorization") String token);

    @DeleteMapping("/api/applications/job/{jobId}")
    void deleteApplicationsByJobId(@PathVariable("jobId") Long jobId, @RequestHeader("Authorization") String token);
}
