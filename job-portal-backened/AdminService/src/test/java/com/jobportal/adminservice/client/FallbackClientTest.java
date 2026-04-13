package com.jobportal.adminservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jobportal.adminservice.dto.response.PageResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FallbackClientTest {

    @Autowired
    private ApplicationServiceClientFallback applicationFallback;

    @Autowired
    private AuthServiceClientFallback authFallback;

    @Autowired
    private JobServiceClientFallback jobFallback;

    @Test
    void applicationFallback_ReturnsZero() {
        ApplicationServiceClient client = applicationFallback.create(new RuntimeException("Test Error"));
        assertThat(client.getTotalApplications()).isEqualTo(0L);
    }

    @Test
    void authFallback_ReturnsEmpty() {
        AuthServiceClient authService = authFallback.create(new RuntimeException("Service down"));
        assertThat(authService.getAllUsers("secret")).isEmpty();
        assertThat(authService.getUserById(1L, "secret")).isNull();
    }

    @Test
    void jobFallback_ReturnsNullOrEmpty() {
        JobServiceClient jobService = jobFallback.create(new RuntimeException("Service down"));
        PageResponse response = jobService.getAllJobs();
        assertThat(response.getContent()).isEmpty();
        assertThat(jobService.getJobById(1L)).isNull();
    }
}
