package com.jobportal.apigateway.filter;

import com.jobportal.apigateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends
        AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            // Check Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization header");
            }

            // Extract token
            String authHeader = request.getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization format");
            }

            String token = authHeader.substring(7);

            // Validate token
            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, "Invalid or expired token");
            }

            // Extract user info and add as headers
            String email  = jwtUtil.extractEmail(token);
            String role   = jwtUtil.extractRole(token);
            Long   userId = jwtUtil.extractUserId(token);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Email", email  != null ? email  : "")
                    .header("X-User-Role",  role   != null ? role   : "")
                    .header("X-User-Id",    userId != null ? userId.toString() : "")
                    .build();

            return chain.filter(exchange.mutate()
                    .request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders()
                .add("Content-Type", "application/json");
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(("{\"error\":\"" + message + "\"}").getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Empty config class required by AbstractGatewayFilterFactory
    }
}