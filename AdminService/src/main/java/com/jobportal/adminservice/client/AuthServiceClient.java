package com.jobportal.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

    @GetMapping("/api/auth/users")
    List<Object> getAllUsers(@RequestHeader("Authorization") String token);

    @GetMapping("/api/auth/users/{id}")
    Object getUserById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @DeleteMapping("/api/auth/users/{id}")
    void deleteUser(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @GetMapping("/api/auth/users/role/{role}")
    List<Object> getUsersByRole(@PathVariable("role") String role, @RequestHeader("Authorization") String token);
}
