package com.starline.resi.feign;

import com.starline.resi.feign.config.FeignBasicConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(name = "${srv.feign.names.whatsapp:whatsapp-svc}", configuration = {FeignBasicConfig.class})
public interface WhatsAppProxySvc {

    @PostMapping(value = "/wa/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void sendMessage(@RequestPart("to") String to, @RequestPart("message") String message);
}
