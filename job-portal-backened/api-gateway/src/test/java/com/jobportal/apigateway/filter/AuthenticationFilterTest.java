package com.jobportal.apigateway.filter;

import com.jobportal.apigateway.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private AuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void apply_MissingAuthorizationHeader_ReturnsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/jobs").build());
        
        filter.apply(new AuthenticationFilter.Config()).filter(exchange, chain).block();

        assert(exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    void apply_InvalidAuthFormat_ReturnsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token")
                .build());
        
        filter.apply(new AuthenticationFilter.Config()).filter(exchange, chain).block();

        assert(exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    void apply_InvalidToken_ReturnsUnauthorized() {
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build());
        
        filter.apply(new AuthenticationFilter.Config()).filter(exchange, chain).block();

        assert(exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    void apply_ValidToken_ForwardsRequestWithHeaders() {
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("test@user.com");
        when(jwtUtil.extractRole(token)).thenReturn("RECRUITER");
        when(jwtUtil.extractUserId(token)).thenReturn(123L);
        
        // Capture the modified exchange
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        when(chain.filter(captor.capture())).thenReturn(Mono.empty());

        MockServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/jobs")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build());
        
        StepVerifier.create(filter.apply(new AuthenticationFilter.Config()).filter(exchange, chain))
                .verifyComplete();

        ServerWebExchange modifiedExchange = captor.getValue();
        assertThat(modifiedExchange.getRequest().getHeaders().getFirst("X-User-Email")).isEqualTo("test@user.com");
        assertThat(modifiedExchange.getRequest().getHeaders().getFirst("X-User-Role")).isEqualTo("RECRUITER");
        assertThat(modifiedExchange.getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("123");
    }
}
