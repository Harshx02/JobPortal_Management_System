package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "JOB-SERVICE")
public interface JobServiceClient {

    @GetMapping("/api/jobs")
    Object getAllJobs(@RequestHeader("Authorization") String token);

    @GetMapping("/api/jobs/recruiter/{recruiterId}")
    List<Object> getJobsByRecruiterId(@PathVariable("recruiterId") Long recruiterId, @RequestHeader("Authorization") String token);

    @DeleteMapping("/api/jobs/recruiter/{recruiterId}")
    void deleteJobsByRecruiterId(@PathVariable("recruiterId") Long recruiterId, @RequestHeader("Authorization") String token);
}
