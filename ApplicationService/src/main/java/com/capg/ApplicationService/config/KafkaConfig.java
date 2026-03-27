package com.capg.ApplicationService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    // Queue names
    public static final String USER_DELETE_REQUESTED_QUEUE = "user-delete-requested";
    public static final String APPLICATION_DELETED_QUEUE = "application-deleted";
    public static final String JOB_APPLIED_QUEUE = "email.job-applied";
    public static final String APPLICATION_STATUS_QUEUE = "email.application-status";

    // Exchange names
    public static final String EXCHANGE = "job-portal-exchange";

    // Declare queues
    @Bean
    public Queue userDeleteRequestedQueue() {
        return new Queue(USER_DELETE_REQUESTED_QUEUE, true);
    }

    @Bean
    public Queue applicationDeletedQueue() {
        return new Queue(APPLICATION_DELETED_QUEUE, true);
    }

    @Bean
    public Queue jobAppliedQueue() {
        return new Queue(JOB_APPLIED_QUEUE, true);
    }

    @Bean
    public Queue applicationStatusQueue() {
        return new Queue(APPLICATION_STATUS_QUEUE, true);
    }

    // Declare exchange
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // Declare bindings
    @Bean
    public Binding bindUserDeleteRequested(Queue userDeleteRequestedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(userDeleteRequestedQueue)
                .to(exchange)
                .with("*.user.delete.requested");
    }

    @Bean
    public Binding bindApplicationDeleted(Queue applicationDeletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(applicationDeletedQueue)
                .to(exchange)
                .with("*.application.deleted");
    }

    @Bean
    public Binding bindJobApplied(Queue jobAppliedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(jobAppliedQueue)
                .to(exchange)
                .with("*.job.applied");
    }

    @Bean
    public Binding bindApplicationStatus(Queue applicationStatusQueue, TopicExchange exchange) {
        return BindingBuilder.bind(applicationStatusQueue)
                .to(exchange)
                .with("*.application.status");
    }
}