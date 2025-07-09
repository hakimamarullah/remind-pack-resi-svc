package com.starline.resi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ResiUpdateResult {
    private String trackingNumber;
    private String newCheckpoint;
    private String originalCheckpointTime;
    private LocalDateTime processedAt;
    private boolean updated;
}

