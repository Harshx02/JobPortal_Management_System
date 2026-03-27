package com.jobportal.jobservice.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.jobportal.jobservice.config.KafkaConfig;
import com.jobportal.jobservice.event.UserDeleteEvent;
import com.jobportal.jobservice.repository.JobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeleteConsumer {

    private final JobRepository jobRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = KafkaConfig.APPLICATION_DELETED_QUEUE)
    public void handle(UserDeleteEvent event) {

        log.info("Received APPLICATION_DELETED | userId: {}", event.getUserId());

        if ("RECRUITER".equalsIgnoreCase(event.getRole())) {

            // ✅ Idempotency check
            if (!jobRepository.existsByRecruiterId(event.getUserId())) {
                log.warn("No jobs found for recruiterId: {}", event.getUserId());
            } else {
                jobRepository.deleteByRecruiterId(event.getUserId());
                log.info("Jobs deleted | recruiterId: {}", event.getUserId());
            }
        }

        // ✅ Update status
        event.setStatus("JOBS_DELETED");

        // ✅ Publish next event
        rabbitTemplate.convertAndSend(KafkaConfig.EXCHANGE, "job.jobs.deleted", event);

        log.info("Published JOBS_DELETED event | userId: {}", event.getUserId());
    }
}