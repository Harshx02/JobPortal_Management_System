package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    @GetMapping("/api/auth/users")
    Object getAllUsers(@RequestHeader("Authorization") String token);
}
