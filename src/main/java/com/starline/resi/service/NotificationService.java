package com.starline.resi.service;

import com.starline.resi.dto.ResiUpdateNotification;

public interface NotificationService {

    void sendCheckpointUpdateNotification(ResiUpdateNotification payload);
}
