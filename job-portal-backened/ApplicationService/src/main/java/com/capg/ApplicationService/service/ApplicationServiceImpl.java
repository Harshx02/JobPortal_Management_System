package com.capg.ApplicationService.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.capg.ApplicationService.client.JobClient;
import com.capg.ApplicationService.client.UserClient;
import com.capg.ApplicationService.config.RabbitMQConfig;
import com.capg.ApplicationService.dto.event.ApplicationStatusEvent;
import com.capg.ApplicationService.dto.event.JobAppliedEvent;
import com.capg.ApplicationService.dto.request.ApplicationRequest;
import com.capg.ApplicationService.dto.response.ApplicationResponse;
import com.capg.ApplicationService.dto.response.JobApplicationResponse;
import com.capg.ApplicationService.dto.response.JobResponse;
import com.capg.ApplicationService.dto.response.UserResponse;
import com.capg.ApplicationService.entity.JobApplication;
import com.capg.ApplicationService.enums.ApplicationStatus;
import com.capg.ApplicationService.exception.ApplicationNotFoundException;
import com.capg.ApplicationService.exception.DuplicateApplicationException;
import com.capg.ApplicationService.exception.UnauthorizedException;
import com.capg.ApplicationService.repository.ApplicationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;
    private final JobClient jobClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${internal.secret}")
    private String internalSecret;

    @Override
    public ApplicationResponse applyForJob(
            ApplicationRequest request, Long userId,
            String role, String resumeUrl) {

        log.info("Apply job service called | userId: {} | jobId: {} | role: {}",
                userId, request.getJobId(), role);

        if (!role.equalsIgnoreCase("JOB_SEEKER")) {
            log.warn("Unauthorized apply attempt | userId: {} | role: {}", userId, role);
            throw new UnauthorizedException(
                    "Access Denied! Only Job Seekers can apply for jobs.");
        }

        JobResponse job;
        try {
            job = jobClient.getJobById(request.getJobId());
            log.debug("Fetched job details | jobId: {}", request.getJobId());
        } catch (Exception e) {
            log.error("Job not found | jobId: {}", request.getJobId(), e);
            throw new RuntimeException(
                    "Job not found with id: " + request.getJobId());
        }

        if (applicationRepository.existsByUserIdAndJobId(
                userId, request.getJobId())) {
            log.warn("Duplicate application attempt | userId: {} | jobId: {}",
                    userId, request.getJobId());
            throw new DuplicateApplicationException(
                    "You have already applied for this job!");
        }

        JobApplication application = new JobApplication();
        application.setUserId(userId);
        application.setJobId(request.getJobId());
        application.setResumeUrl(resumeUrl);

        JobApplication saved =
                applicationRepository.save(application);

        log.info("Application saved | applicationId: {} | userId: {} | jobId: {}",
                saved.getId(), userId, request.getJobId());

        ApplicationResponse response =
                modelMapper.map(saved, ApplicationResponse.class);
        response.setJob(job);

        // Publish Job Applied event
        try {
            UserResponse applicant =
                    userClient.getUserById(userId, internalSecret);
            UserResponse recruiter =
                    userClient.getUserById(job.getRecruiterId(), internalSecret);

            JobAppliedEvent event = new JobAppliedEvent(
                    recruiter.getEmail(),
                    applicant.getName(),
                    applicant.getEmail(),
                    job.getTitle(),
                    job.getCompanyName()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_JOB_APPLIED, event);

            log.info("Job applied event published | applicationId: {}", saved.getId());

        } catch (Exception e) {
            log.error("Failed to publish job applied event | applicationId: {}",
                    saved.getId(), e);
        }

        return response;
    }

    @Override
    public Page<ApplicationResponse> getUserApplications(
            Long userId, String role, Pageable pageable) {

        log.info("Fetching user applications | userId: {} | role: {} | page: {}", 
                userId, role, pageable.getPageNumber());

        if (!role.equalsIgnoreCase("JOB_SEEKER")) {
            log.warn("Unauthorized access to user applications | userId: {} | role: {}", userId, role);
            throw new UnauthorizedException(
                    "Access Denied! Only Job Seekers can view their applications.");
        }

        Page<ApplicationResponse> result = applicationRepository.findByUserId(userId, pageable)
                .map(app -> {
                    ApplicationResponse response =
                            modelMapper.map(app, ApplicationResponse.class);
                    try {
                        JobResponse job = jobClient.getJobById(app.getJobId());
                        response.setJob(job);
                    } catch (Exception e) {
                        log.warn("Job not available | jobId: {}", app.getJobId());
                        JobResponse job = new JobResponse();
                        job.setTitle("Job no longer available");
                        job.setCompanyName("N/A");
                        job.setLocation("N/A");
                        response.setJob(job);
                    }
                    return response;
                });

        log.debug("Applications fetched | userId: {} | totalElements: {}", 
                userId, result.getTotalElements());

        return result;
    }

    @Override
    public Page<JobApplicationResponse> getJobApplications(
            Long jobId, String role, Long recruiterId, Pageable pageable) {

        log.info("Fetching job applications | jobId: {} | recruiterId: {} | page: {}", 
                jobId, recruiterId, pageable.getPageNumber());

        if (!role.equalsIgnoreCase("RECRUITER")) {
            log.warn("Unauthorized access to job applications | recruiterId: {} | role: {}", recruiterId, role);
            throw new UnauthorizedException(
                    "Access Denied! Only Recruiters can view job applicants.");
        }

        JobResponse job = jobClient.getJobById(jobId);

        if (!job.getRecruiterId().equals(recruiterId)) {
            log.warn("Unauthorized job access | jobId: {} | recruiterId: {}", jobId, recruiterId);
            throw new UnauthorizedException(
                    "Access Denied! You can view applications for your own jobs.");
        }

        Page<JobApplicationResponse> result =
                applicationRepository.findByJobId(jobId, pageable)
                        .map(app -> {
                            JobApplicationResponse response = new JobApplicationResponse();
                            response.setId(app.getId());
                            response.setUserId(app.getUserId());
                            response.setJobId(app.getJobId());
                            response.setResumeUrl(app.getResumeUrl());
                            response.setStatus(app.getStatus());
                            response.setAppliedAt(app.getAppliedAt());

                            try {
                                UserResponse user =
                                        userClient.getUserById(app.getUserId(), internalSecret);
                                response.setApplicantName(user.getName());
                                response.setApplicantEmail(user.getEmail());
                            } catch (Exception e) {
                                log.warn("Failed to fetch applicant details | userId: {}", app.getUserId());
                                response.setApplicantName("N/A");
                                response.setApplicantEmail("N/A");
                            }
                            return response;
                        });

        log.debug("Job applications fetched | jobId: {} | totalElements: {}", 
                jobId, result.getTotalElements());

        return result;
    }

    @Override
    public ApplicationResponse updateStatus(
            Long applicationId, ApplicationStatus status,
            Long recruiterId, String role) {

        log.info("Update application status | applicationId: {} | status: {} | recruiterId: {}",
                applicationId, status, recruiterId);

        if (!role.equalsIgnoreCase("RECRUITER")) {
            log.warn("Unauthorized status update attempt | recruiterId: {}", recruiterId);
            throw new UnauthorizedException(
                    "Access Denied! Only Recruiters can update application status.");
        }

        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> {
                    log.error("Application not found | applicationId: {}", applicationId);
                    return new ApplicationNotFoundException(
                            "Application not found with id: " + applicationId);
                });

        JobResponse job = jobClient.getJobById(application.getJobId());

        if (!job.getRecruiterId().equals(recruiterId)) {
            log.warn("Unauthorized status update | applicationId: {} | recruiterId: {}",
                    applicationId, recruiterId);
            throw new UnauthorizedException(
                    "Access Denied! You can only update applications for your own jobs.");
        }

        application.setStatus(status);
        JobApplication updated = applicationRepository.save(application);

        log.info("Application status updated | applicationId: {} | status: {}",
                applicationId, status);

        ApplicationResponse response =
                modelMapper.map(updated, ApplicationResponse.class);

        try {
            JobResponse job1 = jobClient.getJobById(updated.getJobId());
            response.setJob(job1);
        } catch (Exception e) {
            log.warn("Failed to fetch job details | jobId: {}", updated.getJobId());
        }

        // Publish status event
        try {
            UserResponse applicant =
                    userClient.getUserById(updated.getUserId(), internalSecret);
            JobResponse job1 =
                    jobClient.getJobById(updated.getJobId());

            ApplicationStatusEvent event =
                    new ApplicationStatusEvent(
                            applicant.getEmail(),
                            applicant.getName(),
                            job1.getTitle(),
                            job1.getCompanyName(),
                            status.name()
                    );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_APPLICATION_STATUS, event);

            log.info("Application status event published | applicationId: {}", applicationId);

        } catch (Exception e) {
            log.error("Failed to publish status event | applicationId: {}", applicationId, e);
        }

        return response;
    }

    @Override
    public void deleteUserApplications(Long userId) {
        log.info("Deleting applications for userId: {}", userId);
        applicationRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteJobApplications(Long jobId) {
        log.info("Deleting applications for jobId: {}", jobId);
        applicationRepository.deleteByJobId(jobId);
    }

    @Override
    public Long getTotalApplications() {
        Long count = applicationRepository.count();
        log.debug("Total applications count: {}", count);
        return count;
    }

    @Override
    public Long getCountByStatus(ApplicationStatus status, boolean monthly) {
        log.info("Get application count | status: {} | monthly: {}", status, monthly);
        if (monthly) {
            java.time.LocalDateTime lastMonth = java.time.LocalDateTime.now().minusDays(30);
            return applicationRepository.countByStatusAndAppliedAtAfter(status, lastMonth);
        }
        return applicationRepository.countByStatus(status);
    }
}

