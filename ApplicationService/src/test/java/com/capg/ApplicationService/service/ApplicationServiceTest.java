package com.capg.ApplicationService.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.capg.ApplicationService.dto.ApplicationRequest;
import com.capg.ApplicationService.dto.ApplicationResponse;
import com.capg.ApplicationService.entity.Application;
import com.capg.ApplicationService.entity.ApplicationStatus;
import com.capg.ApplicationService.exception.DuplicateApplicationException;
import com.capg.ApplicationService.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository repo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private ApplicationRequest mockRequest;
    private Application mockApplication;
    private ApplicationResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockRequest = new ApplicationRequest();
        mockRequest.setUserId(1L);
        mockRequest.setJobId(101L);

        mockApplication = new Application();
        mockApplication.setId(500L);
        mockApplication.setUserId(1L);
        mockApplication.setJobId(101L);

        mockResponse = new ApplicationResponse();
        mockResponse.setId(500L);
    }

    @Test
    @DisplayName("Should apply for a job successfully")
    void testApply_Success() {
        // Arrange
        when(repo.existsByUserIdAndJobId(1L, 101L)).thenReturn(false);
        when(modelMapper.map(mockRequest, Application.class)).thenReturn(mockApplication);
        when(repo.save(any(Application.class))).thenReturn(mockApplication);
        when(modelMapper.map(any(Application.class), eq(ApplicationResponse.class))).thenReturn(mockResponse);

        // Act
        ApplicationResponse result = applicationService.apply(mockRequest);

        // Assert
        assertNotNull(result);
        assertEquals(500L, result.getId());
        verify(repo, times(1)).save(any(Application.class));
    }

    @Test
    @DisplayName("Should throw exception if application already exists")
    void testApply_DuplicateException() {
        // Arrange
        when(repo.existsByUserIdAndJobId(1L, 101L)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateApplicationException.class, () -> {
            applicationService.apply(mockRequest);
        });

        // Verify save was never called
        verify(repo, never()).save(any(Application.class));
    }

    @Test
    @DisplayName("Should verify cascading delete by user ID")
    void testDeleteByUserId() {
        // Act
        applicationService.deleteByUserId(1L);

        // Assert
        verify(repo, times(1)).deleteByUserId(1L);
    }

    @Test
    @DisplayName("Should verify cascading delete by job ID")
    void testDeleteByJobId() {
        // Act
        applicationService.deleteByJobId(101L);

        // Assert
        verify(repo, times(1)).deleteByJobId(101L);
    }
}