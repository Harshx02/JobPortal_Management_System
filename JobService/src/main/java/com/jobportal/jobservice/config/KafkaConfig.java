package com.jobportal.jobservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    // Queue names - using public static for accessibility
    public static final String USER_DELETE_REQUESTED_QUEUE = "user-delete-requested";
    public static final String APPLICATION_DELETED_QUEUE = "application-deleted";
    public static final String JOBS_DELETED_QUEUE = "jobs-deleted";
    public static final String JOB_POSTED_QUEUE = "email.job-posted";

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
    public Queue jobsDeletedQueue() {
        return new Queue(JOBS_DELETED_QUEUE, true);
    }

    @Bean
    public Queue jobPostedQueue() {
        return new Queue(JOB_POSTED_QUEUE, true);
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
    public Binding bindJobsDeleted(Queue jobsDeletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(jobsDeletedQueue)
                .to(exchange)
                .with("*.jobs.deleted");
    }

    @Bean
    public Binding bindJobPosted(Queue jobPostedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(jobPostedQueue)
                .to(exchange)
                .with("*.job.posted");
    }
}