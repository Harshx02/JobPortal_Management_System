package com.capg.ApplicationService.controller;

import com.capg.ApplicationService.dto.response.ApplicationResponse;
import com.capg.ApplicationService.dto.response.JobApplicationResponse;
import com.capg.ApplicationService.enums.ApplicationStatus;
import com.capg.ApplicationService.service.ApplicationService;
import com.capg.ApplicationService.service.CloudinaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService service;

    @MockBean
    private CloudinaryService cloudinaryService;

    @Test
    void applyForJobs_Success() throws Exception {
        MockMultipartFile resume = new MockMultipartFile("resume", "resume.pdf", "application/pdf",
                "content".getBytes());

        when(cloudinaryService.uploadResume(any())).thenReturn("http://resume.url");
        when(service.applyForJob(any(), anyLong(), anyString(), anyString())).thenReturn(new ApplicationResponse());

        mockMvc.perform(multipart("/api/applications/apply")
                .file(resume)
                .param("jobId", "101")
                .header("X-User-Id", "1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isCreated());
    }

    @Test
    void getUserApplications_Success() throws Exception {
        when(service.getUserApplications(anyLong(), anyString(), any()))
                .thenReturn(new PageImpl<>(List.of(new ApplicationResponse())));

        mockMvc.perform(get("/api/applications/user/viewApplications")
                .header("X-User-Id", "1")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    void getJobApplications_Success() throws Exception {
        when(service.getJobApplications(anyLong(), anyString(), anyLong(), any()))
                .thenReturn(new PageImpl<>(List.of(new JobApplicationResponse())));

        mockMvc.perform(get("/api/applications/jobApplications/101")
                .header("X-User-Id", "202")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    void updatedStatus_Success() throws Exception {
        when(service.updateStatus(anyLong(), any(), anyLong(), anyString())).thenReturn(new ApplicationResponse());

        mockMvc.perform(patch("/api/applications/jobApplication/1/status")
                .param("status", "ACCEPTED")
                .header("X-User-Id", "202")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJobApplications_Success() throws Exception {
        mockMvc.perform(delete("/api/applications/job/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All applications for job deleted successfully!"));
    }

    @Test
    void getTotalApplications_Success() throws Exception {
        when(service.getTotalApplications()).thenReturn(50L);

        mockMvc.perform(get("/api/applications/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }
}
