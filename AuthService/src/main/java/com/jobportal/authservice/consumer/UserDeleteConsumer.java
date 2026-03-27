package com.jobportal.authservice.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.jobportal.authservice.config.KafkaConfig;
import com.jobportal.authservice.event.UserDeleteEvent;
import com.jobportal.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeleteConsumer {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = KafkaConfig.JOBS_DELETED_QUEUE)
    public void handle(UserDeleteEvent event) {

        log.info("Received JOBS_DELETED | userId: {}", event.getUserId());

        // ✅ Idempotency
        if (!userRepository.existsById(event.getUserId())) {
            log.warn("User already deleted | userId: {}", event.getUserId());
        } else {
            userRepository.deleteById(event.getUserId());
            log.info("User deleted | userId: {}", event.getUserId());
        }

        // ✅ Update status
        event.setStatus("COMPLETED");

        // ✅ Publish final event
        rabbitTemplate.convertAndSend(KafkaConfig.EXCHANGE, "auth.user.deleted", event);

        log.info("Published USER_DELETED event | userId: {}", event.getUserId());
    }
}