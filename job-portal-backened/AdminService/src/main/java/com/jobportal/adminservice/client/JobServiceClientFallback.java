package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.jobportal.adminservice.dto.response.JobResponse;
import com.jobportal.adminservice.dto.response.PageResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobServiceClientFallback implements FallbackFactory<JobServiceClient> {

    @Override
    public JobServiceClient create(Throwable cause) {
        return new JobServiceClient() {
            @Override
            public PageResponse getAllJobs() {
            	log.error("JobServiceClient getAllJobs failed - returning empty response", cause);
                return PageResponse.builder()
                    .content(new java.util.ArrayList<>())
                    .totalElements(0L)
                    .totalPages(0)
                    .build();
            }

            @Override
            public JobResponse getJobById(Long id) {
                log.error("JobServiceClient getJobById failed for id: {} - returning null", id, cause);
                return null;
            }

            @Override
            public Long getTotalJobs() {
                log.error("JobServiceClient getTotalJobs failed - returning 0", cause);
                return 0L;
            }
        };
    }
}
