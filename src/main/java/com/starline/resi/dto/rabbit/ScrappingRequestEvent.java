package com.starline.resi.dto.rabbit;


import com.starline.resi.dto.rabbit.enums.ScrappingType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ScrappingRequestEvent {


    private String trackingNumber;

    private String courierCode;

    private String phoneLast5;

    private Long userId;

    private String additionalValue1;

    private ScrappingType type;


}
