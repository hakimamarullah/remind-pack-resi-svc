package com.starline.resi.listener;


import com.starline.resi.config.RabbitMQConfig;
import com.starline.resi.dto.rabbit.ScrappingResultEvent;
import com.starline.resi.dto.rabbit.enums.ScrappingType;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.service.NotificationService;
import com.starline.resi.service.ResiService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQListener {

    private final NotificationService notificationService;

    private final ResiService resiService;


    @RegisterReflectionForBinding
    @WithSpan
    @RabbitListener(queues = {RabbitMQConfig.RESI_SUCCESS_QUEUE})
    public void receiveMessage(@Payload ResiUpdateNotification event) {
        log.info("Receive notification for userId: {} and tracking number: {} {}", event.getUserId(), event.getTrackingNumber(), event.getCourierName());
        notificationService.sendCheckpointUpdateNotification(event);
        log.info("Notification sent for userId: {} {}", event.getUserId(), event.getTrackingNumber());
    }

    @RegisterReflectionForBinding
    @WithSpan
    @RabbitListener(queues = {RabbitMQConfig.SCRAPPING_DONE_QUEUE})
    public void onScrappingDone(@Payload ScrappingResultEvent event) {
        log.info("Receive scrapping result for tracking number: {}", event.getTrackingNumber());
        if (ScrappingType.UPDATE.equals(event.getType())) {
            resiService.handleScrappingUpdateEvent(event);
        } else {
            resiService.handleScrappingResultEvent(event);
        }
        log.info("Successfully handled scrapping result for tracking number: {}", event.getTrackingNumber());
    }
}
