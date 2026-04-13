package com.jobportal.adminservice.exception;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleUnauthorized() {
        UnauthorizedException ex = new UnauthorizedException("Access Denied");
        ResponseEntity<Map<String, String>> response = handler.handleUnauthorized(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().get("error"));
    }

    @Test
    void testHandleNotFound() {
        FeignException.NotFound ex = mock(FeignException.NotFound.class);
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().get("error"));
    }

    @Test
    void testHandleFeignException() {
        FeignException ex = mock(FeignException.class);
        when(ex.getMessage()).thenReturn("Feign Error");
        ResponseEntity<Map<String, String>> response = handler.handleFeignException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("Feign Error"));
    }

    @Test
    void testHandleGeneral() {
        Exception ex = new Exception("General Error");
        ResponseEntity<Map<String, String>> response = handler.handleGeneral(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("General Error"));
    }
}
