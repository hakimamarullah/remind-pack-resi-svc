package com.starline.resi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResiUpdateResult {
    private String trackingNumber;
    private String newCheckpoint;
    private LocalDateTime processedAt;
    private boolean updated;
}

