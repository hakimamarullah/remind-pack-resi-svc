package com.starline.resi.service;

import com.starline.resi.dto.rabbit.ScrappingRequestEvent;
import com.starline.resi.dto.resi.ResiUpdateNotification;

public interface RabbitPublisher {

    void publish(String exchange, String routingKey, Object event);

    void publishResiUpdateNotification(ResiUpdateNotification event);

    void publishScrappingRequest(ScrappingRequestEvent event);
}
