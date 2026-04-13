package com.capg.ApplicationService.consumer;

import com.capg.ApplicationService.config.RabbitMQConfig;
import com.capg.ApplicationService.event.UserDeleteEvent;
import com.capg.ApplicationService.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeleteConsumerTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserDeleteConsumer userDeleteConsumer;

    @Test
    void handle_UserApplicationsExist_DeletesAndPublishes() {
        UserDeleteEvent event = new UserDeleteEvent(1L, "JOB_SEEKER", "PENDING", null);
        when(applicationRepository.existsByUserId(1L)).thenReturn(true);

        userDeleteConsumer.handle(event);

        verify(applicationRepository).deleteByUserId(1L);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.RK_APPLICATION_DELETED), eq(event));
    }

    @Test
    void handle_UserApplicationsDoNotExist_OnlyPublishes() {
        UserDeleteEvent event = new UserDeleteEvent(1L, "JOB_SEEKER", "PENDING", null);
        when(applicationRepository.existsByUserId(1L)).thenReturn(false);

        userDeleteConsumer.handle(event);

        verify(applicationRepository, never()).deleteByUserId(anyLong());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(UserDeleteEvent.class));
    }
}
