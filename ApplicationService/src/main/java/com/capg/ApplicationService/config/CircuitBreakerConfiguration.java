package com.capg.ApplicationService.config;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

@Configuration
public class CircuitBreakerConfiguration {

    @Bean
    public CircuitBreakerFactory circuitBreakerFactory() {
        Resilience4jCircuitBreakerFactory factory = new Resilience4jCircuitBreakerFactory();
        factory.configureDefault(id -> new Resilience4jConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(java.time.Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build())
            .timeLimiterConfig(org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jConfigBuilder.TimeLimiterConfigBuilder
                .of()
                .timeoutDuration(java.time.Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build())
            .build());

        // Add event listeners for monitoring
        factory.addCircuitBreakerCustomizer((circuitBreaker) ->
            circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    System.out.println("CircuitBreaker state transitioned: " + event)), "auth-service");

        factory.addCircuitBreakerCustomizer((circuitBreaker) ->
            circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    System.out.println("CircuitBreaker state transitioned: " + event)), "job-service");

        factory.addCircuitBreakerCustomizer((circuitBreaker) ->
            circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    System.out.println("CircuitBreaker state transitioned: " + event)), "admin-service");

        return factory;
    }
}
