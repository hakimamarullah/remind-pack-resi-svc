package com.starline.resi.dto.resi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ResiUpdateNotification {

    private String trackingNumber;

    private String lastCheckpoint;

    private String previousCheckpoint;

    private String lastCheckpointTime;

    private String previousCheckpointTime;

    private Long userId;

    private String courierName;
}
