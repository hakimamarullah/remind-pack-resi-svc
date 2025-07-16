package com.starline.resi.feign;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.proxy.CekResiScrapResponse;
import com.starline.resi.dto.proxy.ScrappingRequest;
import com.starline.resi.feign.config.FeignBasicConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${srv.feign.names.scrapper:scrapper-svc}",
        configuration = {FeignBasicConfig.class})
public interface ScrapperProxySvc {

    @Retryable(backoff = @Backoff(delay = 4000))
    @PostMapping(value = "/scrapper/tracking", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<CekResiScrapResponse> scrap(@RequestBody ScrappingRequest payload);
}
