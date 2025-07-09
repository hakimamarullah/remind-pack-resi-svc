package com.starline.resi.resources;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WhatsAppTrackingInfoMessage {

    private final MessageSource messageSource;

    @Builder(toBuilder = true)
    public static class TrackingStartParams {
        private String trackingNumber;
        private String currentCheckpoint;
        private String timestamp;

        Object[] toArray() {
            return new Object[]{trackingNumber, currentCheckpoint, timestamp};
        }
    }

    @Builder(toBuilder = true)
    public static class TrackingUpdateParams {
        private String trackingNumber;
        private String currentCheckpoint;
        private String previousCheckpoint;
        private String previousCheckpointTime;
        private String currentCheckpointTime;


        Object[] toArray() {
            return new Object[]{trackingNumber, currentCheckpoint, currentCheckpointTime, previousCheckpoint, previousCheckpointTime};
        }
    }

    public String getTrackingStartMessage(TrackingStartParams params) {
        return messageSource.getMessage("package.tracking.started",
                params.toArray(),
                LocaleContextHolder.getLocale());
    }

    public String getTrackingUpdateMessage(TrackingUpdateParams params) {
        return messageSource.getMessage("package.status.update",
                params.toArray(),
                LocaleContextHolder.getLocale());
    }
}
