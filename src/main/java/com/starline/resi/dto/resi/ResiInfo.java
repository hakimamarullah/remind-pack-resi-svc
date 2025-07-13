package com.starline.resi.dto.resi;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResiInfo {
    private String trackingNumber;
    private Long userId;
    private String courierCode;
    private String lastCheckpoint;
    private String additionalValue1;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastCheckpointUpdate;

    private String courierName;
    private String originalCheckpointTime;
    private Long courierId;
}

