package com.starline.resi.service.impl;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.projection.ResiProjection;
import com.starline.resi.dto.proxy.CekResiScrapResponse;
import com.starline.resi.dto.proxy.ScrappingRequest;
import com.starline.resi.dto.resi.CheckpointUpdateResult;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.feign.ScrapperProxySvc;
import com.starline.resi.service.CheckpointUpdateService;
import com.starline.resi.service.RabbitPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckpointUpdateSvc implements CheckpointUpdateService {

    private final ScrapperProxySvc scrapperProxySvc;

    private final RabbitPublisher publisher;

    @Override
    public ApiResponse<CheckpointUpdateResult> updateCheckpoint(ResiProjection resi) {
        ScrappingRequest request = ScrappingRequest.builder()
                .trackingNumber(resi.getTrackingNumber())
                .courierCode(resi.getCourierCode())
                .phoneLast5(resi.getAdditionalValue1())
                .build();
        ApiResponse<CekResiScrapResponse> response = scrapperProxySvc.scrap(request);
        if (response.isNot2xxSuccessful()) {
            return ApiResponse.setResponse(null, response.getMessage(), response.getCode());
        }

        CekResiScrapResponse data = response.getData();
        log.info("Tracking number: {}, last checkpoint: {}, new checkpoint: {}", resi.getTrackingNumber(), resi.getLastCheckpoint(), data.getCheckpoint());

        boolean updated = !Objects.equals(data.getCheckpoint(), resi.getLastCheckpoint());
        CheckpointUpdateResult result = CheckpointUpdateResult
                .builder()
                .updated(updated)
                .originalCheckpointTime(data.getTimestamp())
                .newCheckpoint(data.getCheckpoint())
                .courierName(resi.getCourierName())
                .build();
        if (updated) {
            ResiUpdateNotification notification = ResiUpdateNotification.builder()
                    .trackingNumber(resi.getTrackingNumber())
                    .userId(resi.getUserId())
                    .previousCheckpoint(resi.getLastCheckpoint())
                    .previousCheckpointTime(resi.getOriginalCheckpointTime())
                    .lastCheckpoint(data.getCheckpoint())
                    .lastCheckpointTime(data.getTimestamp())
                    .courierName(resi.getCourierName())
                    .build();
            publisher.publishSuccessAddResi(notification);
        }
        return ApiResponse.setSuccess(result);
    }
}
