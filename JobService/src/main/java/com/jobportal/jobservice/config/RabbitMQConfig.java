package com.jobportal.jobservice.config;

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

    public static final String JOB_POSTED_QUEUE = "job.posted";
    public static final String APPLICATION_DELETED_QUEUE = "application.deleted";

    // Exchange name
    public static final String EXCHANGE = "job-portal-exchange";

    // Routing Keys
    public static final String RK_JOB_POSTED = "job.job.posted";
    public static final String RK_APPLICATION_DELETED = "app.application.deleted";
    public static final String RK_JOBS_DELETED = "job.jobs.deleted";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue jobPostedQueue() {
        return new Queue(JOB_POSTED_QUEUE, true);
    }

    @Bean
    public Queue applicationDeletedQueue() {
        return new Queue(APPLICATION_DELETED_QUEUE, true);
    }

    @Bean
    public Binding bindApplicationDeleted(Queue applicationDeletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(applicationDeletedQueue)
                .to(exchange)
                .with(RK_APPLICATION_DELETED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory) {
        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}