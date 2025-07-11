package com.starline.resi.dto.proxy;


import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ScrappingRequest {


    private String trackingNumber;

    private String courierCode;

    private String phoneLast5;
}
