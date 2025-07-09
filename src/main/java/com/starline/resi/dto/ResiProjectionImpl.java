package com.starline.resi.dto;

import com.starline.resi.dto.projection.ResiProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ResiProjectionImpl implements ResiProjection {

    private String trackingNumber;
    private Long userId;
    private String lastCheckpoint;
    private String additionalValue1;
    private String courierCode;
    private LocalDateTime lastCheckpointUpdate;
}
