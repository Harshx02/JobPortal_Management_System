package com.capg.ApplicationService.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.capg.ApplicationService.dto.response.UserResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public UserResponse getUserById(Long id, String secret) {
                log.error("UserClient getUserById failed for id: {} - returning null", id, cause);
                return null;
            }
        };
    }
}
