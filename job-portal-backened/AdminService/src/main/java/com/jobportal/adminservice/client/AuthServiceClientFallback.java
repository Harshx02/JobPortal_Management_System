package com.jobportal.adminservice.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.jobportal.adminservice.dto.response.UserResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthServiceClientFallback implements FallbackFactory<AuthServiceClient> {

    @Override
    public AuthServiceClient create(Throwable cause) {
        return new AuthServiceClient() {
            @Override
            public List<UserResponse> getAllUsers(String secret) {
                log.error("AuthServiceClient getAllUsers failed - returning empty list", cause);
                return new ArrayList<>();
            }

            @Override
            public UserResponse getUserById(Long id, String secret) {
                log.error("AuthServiceClient getUserById failed for id: {} - returning null", id, cause);
                return null;
            }

            @Override
            public Long countUsersByRole(String role, String secret) {
                log.error("AuthServiceClient countUsersByRole failed for role: {} - returning 0", role, cause);
                return 0L;
            }
        };
    }
}
