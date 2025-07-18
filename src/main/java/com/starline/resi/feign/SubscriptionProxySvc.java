package com.starline.resi.feign;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.proxy.subscriptions.HasActiveSubscription;
import com.starline.resi.dto.proxy.subscriptions.SubscriptionInfo;
import com.starline.resi.feign.config.FeignBasicConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "${srv.feign.names.subscriptions:subscriptions-svc}", configuration = FeignBasicConfig.class)
public interface SubscriptionProxySvc {

    @GetMapping(value = "/subscriptions/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<SubscriptionInfo>> getSubscriptionInfoByUserId(@PathVariable Long userId);

    @GetMapping(value = "/subscriptions/users/{userId}/has-active-subscription", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<HasActiveSubscription> checkHasActiveSubscription(@PathVariable Long userId);
}
