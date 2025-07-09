package com.starline.resi.batch.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.UUID;

@Component
@Getter
public class BatchInstanceId {

    private final String batchIntanceId;

    public BatchInstanceId() {
        this.batchIntanceId = generateBatchInstanceId();
    }

    public String generateBatchInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" +
                    UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            return "unknown-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

}
