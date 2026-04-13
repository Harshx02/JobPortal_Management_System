package com.jobportal.jobservice.controller;

import com.jobportal.jobservice.dto.JobFilterDto;
import com.jobportal.jobservice.dto.JobRequestDto;
import com.jobportal.jobservice.dto.JobResponseDto;
import com.jobportal.jobservice.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Test
    void createJob_Success() throws Exception {
        JobResponseDto response = new JobResponseDto();
        response.setId(1L);
        when(jobService.createJob(any(JobRequestDto.class), anyLong(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                        .header("X-User-Id", 1L)
                        .header("X-User-Role", "RECRUITER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java Dev\", \"companyName\":\"Google\", \"location\":\"Bangalore\", \"salary\":1000000.0, \"experience\":2, \"description\":\"Desc\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllJobs_Success() throws Exception {
        when(jobService.getAllJobs(anyInt(), anyInt(), anyString(), anyString())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void getJobById_Success() throws Exception {
        when(jobService.getJobById(anyLong())).thenReturn(new JobResponseDto());

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateJob_Success() throws Exception {
        when(jobService.updateJob(anyLong(), any(JobRequestDto.class), anyLong())).thenReturn(new JobResponseDto());

        mockMvc.perform(put("/api/jobs/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java Dev\", \"companyName\":\"Google\", \"location\":\"Bangalore\", \"salary\":1000000.0, \"experience\":2, \"description\":\"Desc\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJob_Success() throws Exception {
        mockMvc.perform(delete("/api/jobs/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void searchJobs_Success() throws Exception {
        when(jobService.searchJobs(any(JobFilterDto.class), anyInt(), anyInt(), anyString(), anyString())).thenReturn(Page.empty());

        mockMvc.perform(post("/api/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteRecruiterJobs_Success() throws Exception {
        mockMvc.perform(delete("/api/jobs/recruiter/1"))
                .andExpect(status().isNoContent());
    }
}
