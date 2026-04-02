package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApplicationServiceClientFallback implements FallbackFactory<ApplicationServiceClient> {

    @Override
    public ApplicationServiceClient create(Throwable cause) {
        return new ApplicationServiceClient() {
            @Override
            public Long getTotalApplications() {
                log.error("ApplicationServiceClient getTotalApplications failed - returning 0", cause);
                return 0L;
            }
        };
    }
}
