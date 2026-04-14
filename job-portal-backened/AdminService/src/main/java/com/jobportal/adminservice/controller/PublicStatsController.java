package com.jobportal.adminservice.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.adminservice.service.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/public")
@RequiredArgsConstructor
public class PublicStatsController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPublicStats() {
        log.info("Public stats API called");
        Map<String, Object> stats = adminService.getPublicStats();
        return ResponseEntity.ok(stats);
    }
}
