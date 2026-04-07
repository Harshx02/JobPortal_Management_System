package com.jobportal.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange Name
    public static final String EXCHANGE = "job-portal-exchange";

    // Queue Names
    public static final String JOB_POSTED_QUEUE = "job.posted";
    public static final String JOB_APPLIED_QUEUE = "job.applied";
    public static final String APPLICATION_STATUS_QUEUE = "application.status";
    public static final String APPLICATION_DELETED_QUEUE = "application.deleted";
    public static final String FORGOT_PASSWORD_QUEUE = "forgot.password.queue";

    // Routing Keys
    public static final String RK_JOB_POSTED = "job.job.posted";
    public static final String RK_JOB_APPLIED = "app.job.applied";
    public static final String RK_APPLICATION_STATUS = "app.application.status.changed";
    public static final String RK_APPLICATION_DELETED = "app.application.deleted";
    public static final String RK_FORGOT_PASSWORD = "auth.forgot.password";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue jobPostedQueue() {
        return new Queue(JOB_POSTED_QUEUE, true);
    }

    @Bean
    public Queue jobAppliedQueue() {
        return new Queue(JOB_APPLIED_QUEUE, true);
    }

    @Bean
    public Queue applicationStatusQueue() {
        return new Queue(APPLICATION_STATUS_QUEUE, true);
    }

    @Bean
    public Queue applicationDeletedQueue() {
        return new Queue(APPLICATION_DELETED_QUEUE, true);
    }

    @Bean
    public Queue forgotPasswordQueue() {
        return new Queue(FORGOT_PASSWORD_QUEUE, true);
    }

    // Bindings
    @Bean
    public Binding bindJobPosted(Queue jobPostedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(jobPostedQueue).to(exchange).with(RK_JOB_POSTED);
    }

    @Bean
    public Binding bindJobApplied(Queue jobAppliedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(jobAppliedQueue).to(exchange).with(RK_JOB_APPLIED);
    }

    @Bean
    public Binding bindApplicationStatus(Queue applicationStatusQueue, TopicExchange exchange) {
        return BindingBuilder.bind(applicationStatusQueue).to(exchange).with(RK_APPLICATION_STATUS);
    }

    @Bean
    public Binding bindApplicationDeleted(Queue applicationDeletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(applicationDeletedQueue).to(exchange).with(RK_APPLICATION_DELETED);
    }

    @Bean
    public Binding bindForgotPassword(Queue forgotPasswordQueue, TopicExchange exchange) {
        return BindingBuilder.bind(forgotPasswordQueue).to(exchange).with(RK_FORGOT_PASSWORD);
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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
