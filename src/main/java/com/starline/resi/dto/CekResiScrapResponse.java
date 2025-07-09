package com.starline.resi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CekResiScrapResponse {

    private String timestamp;
    private String checkpoint;

}
