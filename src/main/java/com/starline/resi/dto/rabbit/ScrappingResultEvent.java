package com.starline.resi.dto.rabbit;

import com.starline.resi.dto.rabbit.enums.ScrappingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ScrappingResultEvent {

    private String trackingNumber;
    private String timestamp;
    private String checkpoint;
    private String courierCode;
    private Long userId;
    private String additionalValue1;
    private ScrappingType type;
}
