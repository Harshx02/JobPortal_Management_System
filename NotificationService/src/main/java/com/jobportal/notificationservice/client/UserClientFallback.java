package com.jobportal.notificationservice.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.jobportal.notificationservice.dto.UserResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public List<UserResponse> getAllUsers(String secret) {
                log.error("UserClient getAllUsers failed - returning empty list", cause);
                return new ArrayList<>();
            }
        };
    }
}
