package com.starline.resi.service.impl;


import com.starline.resi.config.RabbitMQConfig;
import com.starline.resi.dto.rabbit.ScrappingRequestEvent;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.service.RabbitPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@RegisterReflectionForBinding({
        ResiUpdateNotification.class,
        ScrappingRequestEvent.class

})
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
    public void publishResiUpdateNotification(ResiUpdateNotification event) {
        publish(
                RabbitMQConfig.RESI_EXCHANGE,
                RabbitMQConfig.RESI_UPDATE_NOTIFICATION,
                event
        );
    }


    @Async
    @Retryable(backoff = @Backoff(delay = 3000), maxAttempts = 5)
    @Override
    public void publishScrappingRequest(ScrappingRequestEvent event) {
         publish(
                 RabbitMQConfig.SCRAPPING_EXCHANGE,
                 RabbitMQConfig.SCRAPPING_REQUEST_ROUTING_KEY,
                 event
         );
    }
}
