package com.jobportal.authservice.config;

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

    // Queue name
    public static final String JOBS_DELETED_QUEUE = "jobs.deleted";
    public static final String FORGOT_PASSWORD_QUEUE = "forgot.password.queue";

    // Exchange name
    public static final String EXCHANGE = "job-portal-exchange";

    // Routing Keys
    public static final String RK_JOBS_DELETED = "job.jobs.deleted";
    public static final String RK_USER_DELETED = "auth.user.deleted";
    public static final String RK_FORGOT_PASSWORD = "auth.forgot.password";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue jobsDeletedQueue() {
        return new Queue(JOBS_DELETED_QUEUE, true);
    }

    @Bean
    public Binding bindJobsDeleted(Queue jobsDeletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(jobsDeletedQueue)
                .to(exchange)
                .with(RK_JOBS_DELETED);
    }

    @Bean
    public Queue forgotPasswordQueue() {
        return new Queue(FORGOT_PASSWORD_QUEUE, true);
    }

    @Bean
    public Binding bindForgotPassword(Queue forgotPasswordQueue, TopicExchange exchange) {
        return BindingBuilder.bind(forgotPasswordQueue)
                .to(exchange)
                .with(RK_FORGOT_PASSWORD);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    //producer
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
