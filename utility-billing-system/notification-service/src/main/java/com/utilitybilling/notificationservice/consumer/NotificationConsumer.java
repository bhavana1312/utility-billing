package com.utilitybilling.notificationservice.consumer;

import com.utilitybilling.notificationservice.dto.NotificationEventDTO;
import com.utilitybilling.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer{

    private final NotificationService notificationService;

    @RabbitListener(queues="notification.queue")
    public void consume(NotificationEventDTO event){
        notificationService.send(event);
    }
}
