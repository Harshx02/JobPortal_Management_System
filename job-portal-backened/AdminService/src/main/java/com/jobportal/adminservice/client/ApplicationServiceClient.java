package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "application-service", fallbackFactory = ApplicationServiceClientFallback.class)
public interface ApplicationServiceClient {

    @GetMapping("/api/applications/count")
    Long getTotalApplications();

    @GetMapping("/api/applications/count-by-status")
    Long getCountByStatus(@org.springframework.web.bind.annotation.RequestParam("status") String status,
            @org.springframework.web.bind.annotation.RequestParam("monthly") boolean monthly);
}