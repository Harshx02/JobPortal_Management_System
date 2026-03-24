package com.jobportal.adminservice.service;

import com.jobportal.adminservice.client.AuthServiceClient;
import com.jobportal.adminservice.client.JobServiceClient;
import com.jobportal.adminservice.client.ApplicationServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private JobServiceClient jobServiceClient;

    @Autowired
    private ApplicationServiceClient applicationServiceClient;

    public void deleteUserCascading(Long userId, String token) {
        // 1. Get user details to check role
        Object userObj = authServiceClient.getUserById(userId, token);
        // Assuming userObj is a Map or has a way to get role
        // For simplicity, let's cast to Map if it's returned as such by Feign
        Map<String, Object> user = (Map<String, Object>) userObj;
        String role = (String) user.get("role");

        if ("JOB_SEEKER".equals(role)) {
            // Delete applications by user
            applicationServiceClient.deleteApplicationsByUserId(userId, token);
        } else if ("RECRUITER".equals(role)) {
            // 1. Get all jobs by this recruiter
            List<Object> jobs = jobServiceClient.getJobsByRecruiterId(userId, token);
            for (Object jobObj : jobs) {
                Map<String, Object> job = (Map<String, Object>) jobObj;
                Long jobId = ((Number) job.get("id")).longValue();
                // 2. Delete applications for each job
                applicationServiceClient.deleteApplicationsByJobId(jobId, token);
            }
            // 3. Delete all jobs by recruiter
            jobServiceClient.deleteJobsByRecruiterId(userId, token);
        }

        // Finally, delete the user from AuthService
        authServiceClient.deleteUser(userId, token);
    }
}
