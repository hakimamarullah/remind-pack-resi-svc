package com.starline.resi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ResiUpdateNotification {

    private String trackingNumber;

    private String lastCheckpoint;

    private String previousCheckpoint;

    private String lastCheckpointTime;

    private String previousCheckpointTime;

    private Long userId;
}
