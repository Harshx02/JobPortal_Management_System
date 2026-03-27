package com.jobportal.adminservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String USER_DELETE_REQUESTED_QUEUE = "user-delete-requested";

    // Exchange names
    public static final String EXCHANGE = "job-portal-exchange";

    // Declare queues
    @Bean
    public Queue userDeleteRequestedQueue() {
        return new Queue(USER_DELETE_REQUESTED_QUEUE, true);
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
}
