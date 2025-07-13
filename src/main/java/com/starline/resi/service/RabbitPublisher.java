package com.starline.resi.service;

import com.starline.resi.dto.resi.ResiUpdateNotification;

public interface RabbitPublisher {

    void publish(String exchange, String routingKey, Object event);

    void publishSuccessAddResi(ResiUpdateNotification event);
}
