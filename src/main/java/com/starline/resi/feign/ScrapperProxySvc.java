package com.starline.resi.feign;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.resi.CekResiScrapResponse;
import com.starline.resi.dto.proxy.ScrappingRequest;
import com.starline.resi.feign.config.FeignBasicConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "scrapper",
        url = "${url.svc.scrapper:http://localhost:8081}",
        configuration = {FeignBasicConfig.class})
public interface ScrapperProxySvc {

    @PostMapping(value = "/scrapper/tracking", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<CekResiScrapResponse> scrap(@RequestBody ScrappingRequest payload);
}
