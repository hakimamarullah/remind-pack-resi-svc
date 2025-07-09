package com.starline.resi.dto.projection;

import java.time.LocalDateTime;

public interface RegOTPSimpleData {

    String getCode();

    String getMobilePhone();

    LocalDateTime getCreatedDate();
}
