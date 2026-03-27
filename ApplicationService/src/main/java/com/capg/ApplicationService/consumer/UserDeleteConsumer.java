package com.capg.ApplicationService.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.capg.ApplicationService.repository.ApplicationRepository;
import com.capg.ApplicationService.event.UserDeleteEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeleteConsumer {

    private final ApplicationRepository applicationRepository;
    private final KafkaTemplate<String, UserDeleteEvent> kafkaTemplate;

    @KafkaListener(
            topics = "user-delete-requested",
            groupId = "application-group"
    )
    public void handle(UserDeleteEvent event) {

        log.info("Received USER_DELETE_REQUESTED | userId: {}", event.getUserId());

        // ✅ Idempotency check
        if (!applicationRepository.existsByUserId(event.getUserId())) {
            log.warn("No applications found for userId: {}", event.getUserId());
        } else {
            applicationRepository.deleteByUserId(event.getUserId());
            log.info("Applications deleted | userId: {}", event.getUserId());
        }

        // ✅ Update status
        event.setStatus("APPLICATION_DELETED");

        // ✅ Publish next event
        kafkaTemplate.send("application-deleted", event);

        log.info("Published APPLICATION_DELETED event | userId: {}", event.getUserId());
    }
}