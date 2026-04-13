package com.capg.ApplicationService.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FallbackClientTest {

    @Autowired
    private JobClientFallback jobFallback;

    @Autowired
    private UserClientFallback userFallback;

    @Test
    void jobClientFallback_ReturnsNull() {
        JobClient client = jobFallback.create(new RuntimeException("Test"));
        assertThat(client.getJobById(1L)).isNull();
    }

    @Test
    void userClientFallback_ReturnsNull() {
        UserClient client = userFallback.create(new RuntimeException("Test"));
        assertThat(client.getUserById(1L, "secret")).isNull();
    }
}
