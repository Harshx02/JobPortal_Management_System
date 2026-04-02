package com.capg.ApplicationService.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.capg.ApplicationService.dto.response.JobResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobClientFallback implements FallbackFactory<JobClient> {

    @Override
    public JobClient create(Throwable cause) {
        return new JobClient() {
            @Override
            public JobResponse getJobById(Long id) {
                log.error("JobClient getJobById failed for id: {} - returning null", id, cause);
                return null;
            }
        };
    }
}
