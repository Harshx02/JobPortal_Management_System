package com.jobportal.adminservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
class ConfigTest {

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @MockBean
    private ConnectionFactory connectionFactory;

    @Test
    void rabbitMQConfig_BeansCreated() {
        Queue queue = rabbitMQConfig.userDeleteRequestedQueue();
        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.USER_DELETE_REQUESTED_QUEUE);

        TopicExchange exchange = rabbitMQConfig.exchange();
        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.EXCHANGE);

        Binding binding = rabbitMQConfig.bindUserDeleteRequested(queue, exchange);
        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.EXCHANGE);
        assertThat(binding.getRoutingKey()).isEqualTo("admin.user.delete.requested");

        Jackson2JsonMessageConverter converter = rabbitMQConfig.messageConverter();
        assertThat(converter).isNotNull();

        RabbitTemplate template = rabbitMQConfig.rabbitTemplate(connectionFactory);
        assertThat(template).isNotNull();
    }
}
