package com.starline.resi.dto.proxy;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UserInfo {

    private Long id;

    private String mobilePhone;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime lastLogin;

    private Boolean enabled;
}
