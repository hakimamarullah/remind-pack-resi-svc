package com.starline.resi.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class MessageSourceConfig {

    @Bean
    @Primary
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/msg");
        source.setDefaultLocale(Locale.ENGLISH);
        source.setFallbackToSystemLocale(false);
        source.setDefaultEncoding("UTF-8");
        return source;
    }



}
