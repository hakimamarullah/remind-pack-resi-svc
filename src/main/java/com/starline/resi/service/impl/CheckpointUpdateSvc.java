package com.starline.resi.service.impl;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.CekResiScrapResponse;
import com.starline.resi.dto.CheckpointUpdateResult;
import com.starline.resi.dto.ScrappingRequest;
import com.starline.resi.dto.projection.ResiProjection;
import com.starline.resi.feign.ScrapperProxySvc;
import com.starline.resi.service.CheckpointUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckpointUpdateSvc implements CheckpointUpdateService {

    private final ScrapperProxySvc scrapperProxySvc;

    @Override
    public ApiResponse<CheckpointUpdateResult> updateCheckpoint(ResiProjection resi) {
        ScrappingRequest request = ScrappingRequest.builder()
                .trackingNumber(resi.getTrackingNumber())
                .courierCode(resi.getCourierCode())
                .phoneLast5(resi.getAdditionalValue1())
                .build();
        ApiResponse<CekResiScrapResponse> response = scrapperProxySvc.scrap(request);
        if (!response.is2xxSuccessful()) {
            return ApiResponse.setResponse(null, response.getMessage(), response.getCode());
        }

        CekResiScrapResponse data = response.getData();
        log.info("Tracking number: {}, last checkpoint: {}, new checkpoint: {}", resi.getTrackingNumber(), resi.getLastCheckpoint(), data.getCheckpoint());

        boolean updated = Objects.equals(data.getCheckpoint(), resi.getLastCheckpoint());
        CheckpointUpdateResult result = CheckpointUpdateResult
                .builder()
                .updated(updated)
                .newCheckpoint(data.getCheckpoint())
                .build();
        return ApiResponse.setSuccess(result);
    }
}
