package com.capg.ApplicationService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String USER_DELETE_REQUESTED_QUEUE = "user-delete-requested";
    public static final String JOB_APPLIED_QUEUE = "job.applied";
    public static final String APPLICATION_STATUS_QUEUE = "application.status";
    public static final String APPLICATION_DELETED_QUEUE = "application.deleted";

    // Exchange name
    public static final String EXCHANGE = "job-portal-exchange";

    // Routing Keys (matching what publishers use)
    public static final String RK_USER_DELETE_REQUESTED = "admin.user.delete.requested";
    public static final String RK_JOB_APPLIED = "app.job.applied";
    public static final String RK_APPLICATION_STATUS = "app.application.status.changed";
    public static final String RK_APPLICATION_DELETED = "app.application.deleted";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue userDeleteRequestedQueue() {
        return new Queue(USER_DELETE_REQUESTED_QUEUE, true);
    }

    @Bean
    public Binding bindUserDeleteRequested(Queue userDeleteRequestedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(userDeleteRequestedQueue)
                .to(exchange)
                .with(RK_USER_DELETE_REQUESTED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}