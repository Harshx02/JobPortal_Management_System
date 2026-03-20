package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.dto.StatusUpdateRequest;
import com.capg.ApplicationService.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;

    // APPLY
    @PostMapping("/apply")
    public ResponseEntity<ApplicationResponse> apply(@RequestBody ApplicationRequest request) {
        ApplicationResponse response = service.apply(request);
        return ResponseEntity.ok(response);
    }

    // GET USER APPLICATIONS
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponse>> getUserApps(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUserApplications(userId));
    }

    // GET JOB APPLICATIONS
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getJobApps(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.getJobApplicants(jobId));
    }

    // UPDATE STATUS
    @PutMapping("/status")
    public ResponseEntity<ApplicationResponse> updateStatus(@RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(service.updateStatus(request));
    }
}