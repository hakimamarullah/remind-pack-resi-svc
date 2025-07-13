package com.starline.resi.dto.rabbit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ResiAddedEvent {

    private Long userId;

    private String trackingNumber;

    private Long courierId;
}
