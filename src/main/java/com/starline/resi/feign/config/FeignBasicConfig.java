package com.starline.resi.feign.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignBasicConfig {

    @Bean
    Logger.Level loggerInfo() {
        return Logger.Level.BASIC;
    }

}
