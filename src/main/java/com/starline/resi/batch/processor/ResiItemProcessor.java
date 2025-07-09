package com.starline.resi.batch.processor;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.CheckpointUpdateResult;
import com.starline.resi.dto.ResiUpdateResult;
import com.starline.resi.dto.projection.ResiProjection;
import com.starline.resi.exceptions.ApiException;
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
public class ResiItemProcessor implements ItemProcessor<ResiProjection, ResiUpdateResult> {

    private final CheckpointUpdateService checkpointUpdateService;
    private final MeterRegistry meterRegistry;


    @Override
    public ResiUpdateResult process(ResiProjection item) throws Exception {
        try {
            ApiResponse<CheckpointUpdateResult> result = checkpointUpdateService.updateCheckpoint(item);

            if (!result.is2xxSuccessful()) {
                throw new ApiException(result.getMessage());
            }

            var responseData = result.getData();
            meterRegistry.counter("resi.processing.processed").increment();

            if (responseData.isUpdated()) {
                meterRegistry.counter("resi.processing.updated").increment();
                return new ResiUpdateResult(item.getTrackingNumber(), responseData.getNewCheckpoint(),
                        LocalDateTime.now(), true);
            } else {
                meterRegistry.counter("resi.processing.skipped").increment();
                return new ResiUpdateResult(item.getTrackingNumber(), item.getLastCheckpoint(),
                        LocalDateTime.now(), false);
            }

        } catch (Exception e) {
            log.error("Error processing tracking number {}: {}",
                    item.getTrackingNumber(), e.getMessage());
            meterRegistry.counter("resi.processing.failed").increment();
            throw e; // Let Spring Batch handle retry logic
        }
    }
}

