package com.jobportal.adminservice.controller;

import com.jobportal.adminservice.client.AuthServiceClient;
import com.jobportal.adminservice.client.JobServiceClient;
import com.jobportal.adminservice.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private JobServiceClient jobServiceClient;

    @Autowired
    private AdminService adminService;

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            List<Object> users = authServiceClient.getAllUsers(token != null ? token : "");
            return ResponseEntity.ok(Map.of("status", "success", "data", users));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching users: " + e.getMessage()));
        }
    }

    // GET /api/admin/users/{id}
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Object user = authServiceClient.getUserById(id, token != null ? token : "");
            return ResponseEntity.ok(Map.of("status", "success", "data", user));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found or error occurred: " + e.getMessage()));
        }
    }

    // GET /api/admin/job-seekers
    @GetMapping("/job-seekers")
    public ResponseEntity<?> getJobSeekers(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            List<Object> seekers = authServiceClient.getUsersByRole("JOB_SEEKER", token != null ? token : "");
            return ResponseEntity.ok(Map.of("status", "success", "data", seekers));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching job seekers: " + e.getMessage()));
        }
    }

    // GET /api/admin/jobs
    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Object jobs = jobServiceClient.getAllJobs(token != null ? token : "");
            return ResponseEntity.ok(Map.of("status", "success", "data", jobs));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching jobs: " + e.getMessage()));
        }
    }

    // DELETE /api/admin/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            adminService.deleteUserCascading(id, token != null ? token : "");
            return ResponseEntity.ok(Map.of("status", "success", "message", "User deleted successfully with all related records"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error deleting user: " + e.getMessage()));
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
