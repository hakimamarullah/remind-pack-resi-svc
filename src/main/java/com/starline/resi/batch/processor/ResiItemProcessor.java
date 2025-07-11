package com.starline.resi.batch.processor;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.resi.CheckpointUpdateResult;
import com.starline.resi.dto.resi.ResiUpdateResult;
import com.starline.resi.dto.projection.ResiProjection;
import com.starline.resi.exceptions.ApiException;
import com.starline.resi.model.Resi;
import com.starline.resi.service.CheckpointUpdateService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ResiItemProcessor implements ItemProcessor<Resi, ResiUpdateResult> {

    private final CheckpointUpdateService checkpointUpdateService;
    private final MeterRegistry meterRegistry;


    @Override
    public ResiUpdateResult process(Resi item) {
        try {
            ApiResponse<CheckpointUpdateResult> result = checkpointUpdateService.updateCheckpoint(toResiProjection(item));

            if (result.isNot2xxSuccessful()) {
                throw new ApiException(result.getMessage());
            }

            var responseData = result.getData();
            meterRegistry.counter("resi.processing.processed").increment();

            if (responseData.isUpdated()) {
                meterRegistry.counter("resi.processing.updated").increment();
                return ResiUpdateResult.builder()
                        .trackingNumber(item.getTrackingNumber())
                        .newCheckpoint(responseData.getNewCheckpoint())
                        .originalCheckpointTime(responseData.getOriginalCheckpointTime())
                        .updated(true)
                        .processedAt(LocalDateTime.now())
                        .build();

            } else {
                meterRegistry.counter("resi.processing.skipped").increment();
                return ResiUpdateResult.builder()
                        .trackingNumber(item.getTrackingNumber())
                        .newCheckpoint(item.getLastCheckpoint())
                        .originalCheckpointTime(item.getOriginalCheckpointTime())
                        .updated(false)
                        .processedAt(LocalDateTime.now())
                        .build();
            }

        } catch (Exception e) {
            log.error("Error processing tracking number {}: {}",
                    item.getTrackingNumber(), e.getMessage());
            meterRegistry.counter("resi.processing.failed").increment();
            throw e; // Let Spring Batch handle retry logic
        }
    }

    public ResiProjection toResiProjection(Resi item) {
        return new ResiProjection() {
            @Override
            public String getTrackingNumber() {
                return item.getTrackingNumber();
            }

            @Override
            public Long getUserId() {
                return item.getUserId();
            }

            @Override
            public String getCourierCode() {
                return item.getCourier().getCode();
            }

            @Override
            public String getLastCheckpoint() {
                return item.getLastCheckpoint();
            }

            @Override
            public String getAdditionalValue1() {
                return item.getAdditionalValue1();
            }

            @Override
            public LocalDateTime getLastCheckpointUpdate() {
                return item.getLastCheckpointUpdate();
            }

            @Override
            public String getCourierName() {
                return item.getCourier().getName();
            }

            @Override
            public String getOriginalCheckpointTime() {
                return item.getOriginalCheckpointTime();
            }

            @Override
            public Long getCourierId() {
                return item.getCourier().getId();
            }
        };
    }
}

