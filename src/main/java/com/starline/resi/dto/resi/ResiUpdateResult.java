package com.starline.resi.dto.resi;

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

    private LocalDateTime processedAt;

    private String trackingNumber;

}

