package com.jobportal.authservice.consumer;

import com.jobportal.authservice.config.RabbitMQConfig;
import com.jobportal.authservice.event.UserDeleteEvent;
import com.jobportal.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeleteConsumerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserDeleteConsumer userDeleteConsumer;

    @Test
    void handle_UserExists_DeletesAndPublishes() {
        UserDeleteEvent event = new UserDeleteEvent(1L, "admin", "PENDING", null);
        when(userRepository.existsById(1L)).thenReturn(true);

        userDeleteConsumer.handle(event);

        verify(userRepository).deleteById(1L);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.EXCHANGE), eq(RabbitMQConfig.RK_USER_DELETED), eq(event));
    }

    @Test
    void handle_UserDoesNotExist_OnlyPublishes() {
        UserDeleteEvent event = new UserDeleteEvent(1L, "admin", "PENDING", null);
        when(userRepository.existsById(1L)).thenReturn(false);

        userDeleteConsumer.handle(event);

        verify(userRepository, never()).deleteById(anyLong());
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(UserDeleteEvent.class));
    }
}
