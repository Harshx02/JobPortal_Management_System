package com.jobportal.adminservice.controller;

import com.jobportal.adminservice.client.AuthServiceClient;
import com.jobportal.adminservice.client.JobServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private JobServiceClient jobServiceClient;

    @GetMapping("/users")
    public ResponseEntity<?> manageUsers(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Object users = authServiceClient.getAllUsers(token != null ? token : "");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", users
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Auth Service unavailable or error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<?> manageJobs(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Object jobs = jobServiceClient.getAllJobs(token != null ? token : "");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", jobs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Job Service unavailable or error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Admin reports endpoint operational",
            "data", Map.of(
                 "activeUsers", 1500,
                 "activeJobs", 340,
                 "applicationsToday", 120
            )
        ));
    }
}
