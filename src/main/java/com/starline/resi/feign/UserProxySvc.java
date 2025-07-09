package com.starline.resi.feign;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.UserInfo;
import com.starline.resi.feign.config.FeignBasicConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-svc", url = "${url.svc.user:http://localhost:8080}", configuration = FeignBasicConfig.class)
public interface UserProxySvc  {

    @GetMapping(value = "/users/info/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserInfo> getUserInfoById(@PathVariable Long id);

    @GetMapping(value = "/users/info/phone/{mobilePhone}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserInfo> getUserInfoByMobilePhone(@PathVariable String mobilePhone);
}
