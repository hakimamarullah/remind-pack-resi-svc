package com.starline.resi.service.impl;


import com.starline.resi.config.RabbitMQConfig;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.service.RabbitPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQPublisher implements RabbitPublisher {


    private final RabbitTemplate rabbitTemplate;

    @Async
    @Retryable(backoff = @Backoff(delay = 5000))
    @Override
    public void publish(String exchange, String routingKey, Object event) {
        log.info("Publishing event to exchange: {}, routing key: {}", exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Event published to exchange: {}, routing key: {}", exchange, routingKey);
    }

    @Async
    @Retryable(backoff = @Backoff(delay = 5000))
    @Override
    public void publishSuccessAddResi(ResiUpdateNotification event) {
        publish(
                RabbitMQConfig.RESI_EXCHANGE,
                RabbitMQConfig.RESI_SUCCESS_ADD_ROUTING_KEY,
                event
        );
    }
}
