package com.jobportal.notificationservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FallbackClientTest {

    @Autowired
    private UserClientFallback userFallback;

    @Test
    void userClientFallback_ReturnsEmpty() {
        UserClient client = userFallback.create(new RuntimeException("Test Error"));
        assertThat(client.getAllUsers("secret")).isEmpty();
    }
}
