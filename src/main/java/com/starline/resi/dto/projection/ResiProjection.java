package com.starline.resi.dto.projection;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public interface ResiProjection {

    String getTrackingNumber();

    Long getUserId();

    String getCourierCode();

    String getLastCheckpoint();

    String getAdditionalValue1();

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    LocalDateTime getLastCheckpointUpdate();

    String getCourierName();

    String getOriginalCheckpointTime();

    Long getCourierId();
}
