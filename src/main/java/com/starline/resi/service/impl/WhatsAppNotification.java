package com.starline.resi.service.impl;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.dto.proxy.UserInfo;
import com.starline.resi.exceptions.ApiException;
import com.starline.resi.feign.UserProxySvc;
import com.starline.resi.feign.WhatsAppProxySvc;
import com.starline.resi.resources.WhatsAppTrackingInfoMessage;
import com.starline.resi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppNotification implements NotificationService {

    private final WhatsAppProxySvc whatsAppProxySvc;

    private final WhatsAppTrackingInfoMessage waTrackingInfoMessage;

    private final UserProxySvc userProxySvc;

    @Async
    @Retryable(backoff = @Backoff(delay = 2000))
    @Override
    public void sendCheckpointUpdateNotification(ResiUpdateNotification payload) {
        ApiResponse<UserInfo> userInfoResponse = userProxySvc.getUserInfoById(payload.getUserId());
        if (userInfoResponse.isNot2xxSuccessful()) {
            throw new ApiException(userInfoResponse.getMessage());
        }

        var userInfo = userInfoResponse.getData();
        String message;
        if (!Objects.isNull(payload.getPreviousCheckpoint())) {
            WhatsAppTrackingInfoMessage.TrackingUpdateParams params = WhatsAppTrackingInfoMessage.TrackingUpdateParams.builder()
                    .trackingNumber(payload.getTrackingNumber())
                    .currentCheckpoint(payload.getLastCheckpoint())
                    .currentCheckpointTime(payload.getLastCheckpointTime())
                    .previousCheckpoint(payload.getPreviousCheckpoint())
                    .previousCheckpointTime(payload.getPreviousCheckpointTime())
                    .build();
            message = waTrackingInfoMessage.getTrackingUpdateMessage(params);
        } else {
            WhatsAppTrackingInfoMessage.TrackingStartParams params = WhatsAppTrackingInfoMessage.TrackingStartParams.builder()
                    .trackingNumber(payload.getTrackingNumber())
                    .currentCheckpoint(payload.getLastCheckpoint())
                    .timestamp(payload.getLastCheckpointTime())
                    .build();
            message = waTrackingInfoMessage.getTrackingStartMessage(params);
        }

        if (StringUtils.isBlank(message)) {
            return;
        }
        log.info("Sending notification to {}", userInfo.getMobilePhone());
        whatsAppProxySvc.sendMessage(userInfo.getMobilePhone(), message);
        log.info("Notification sent to {}", userInfo.getMobilePhone());
    }
}
