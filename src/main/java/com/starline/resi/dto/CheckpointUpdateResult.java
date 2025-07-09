package com.starline.resi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CheckpointUpdateResult {
    private String newCheckpoint;
    private boolean updated;
}
