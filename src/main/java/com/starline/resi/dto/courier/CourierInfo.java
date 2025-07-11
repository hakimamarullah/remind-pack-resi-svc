package com.starline.resi.dto.courier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CourierInfo {

    private Long id;
    private String code;
    private String name;
}
