package com.starline.resi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "resi.batch")
@Data
@Component
public class ResiBatchProperties {
    private int chunkSize = 1000;
    private int threadPoolSize = 5;
    private int retryLimit = 3;
    private int skipLimit = 100;
    private int throttleLimit = 5;
    private boolean enabled = true;
    private int processingIntervalHours = 4;
    private int partitionSize = 5;
}
