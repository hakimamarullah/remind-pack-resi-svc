package com.starline.resi.config;


import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "redis.cache")
@Component
@Getter
public class RedisCacheProp {

    private String host = "localhost";
    private int port = 6379;

    private long courierTtlHours = 3L;
    private long resiTtlMinutes = 3L;
}
