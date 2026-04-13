package com.jobportal.adminservice.controller;

import com.jobportal.adminservice.dto.response.JobResponse;
import com.jobportal.adminservice.dto.response.PageResponse;
import com.jobportal.adminservice.dto.response.UserResponse;
import com.jobportal.adminservice.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    void getAllUsers_AdminSuccess() throws Exception {
        when(adminService.getAllUsers()).thenReturn(List.of(new UserResponse()));
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_AdminSuccess() throws Exception {
        when(adminService.getUserById(anyLong())).thenReturn(new UserResponse());
        mockMvc.perform(get("/api/admin/users/1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users/1")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_AdminSuccess() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isAccepted());
    }

    @Test
    void deleteUser_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllJobs_AdminSuccess() throws Exception {
        when(adminService.getAllJobs()).thenReturn(new PageResponse());
        mockMvc.perform(get("/api/admin/jobs")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllJobs_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/jobs")
                        .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getJobById_AdminSuccess() throws Exception {
        when(adminService.getJobById(anyLong())).thenReturn(new JobResponse());
        mockMvc.perform(get("/api/admin/jobs/101")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getJobById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/jobs/101")
                        .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReports_AdminSuccess() throws Exception {
        when(adminService.getReports()).thenReturn(Map.of("totalUsers", 10));
        mockMvc.perform(get("/api/admin/reports")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getReports_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/reports")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());
    }
}
