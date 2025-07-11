package com.starline.resi.dto.projection;

import java.time.LocalDateTime;

public interface ResiProjection {

    String getTrackingNumber();

    Long getUserId();

    String getCourierCode();

    String getLastCheckpoint();

    String getAdditionalValue1();

    LocalDateTime getLastCheckpointUpdate();

    String getCourierName();

    String getOriginalCheckpointTime();

    Long getCourierId();
}
