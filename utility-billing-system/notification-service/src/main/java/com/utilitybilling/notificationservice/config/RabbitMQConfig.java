package com.utilitybilling.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig{

    public static final String EXCHANGE_NAME="notification.exchange";
    public static final String QUEUE_NAME="notification.queue";
    public static final String ROUTING_KEY="notification.send";

    @Bean
    public DirectExchange notificationExchange(){
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationQueue(){
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding notificationBinding(){
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(ROUTING_KEY);
    }
}
