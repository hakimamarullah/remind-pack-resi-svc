package com.starline.resi.batch.processor;

import com.starline.resi.dto.rabbit.ScrappingRequestEvent;
import com.starline.resi.dto.rabbit.enums.ScrappingType;
import com.starline.resi.dto.resi.ResiUpdateResult;
import com.starline.resi.model.Resi;
import com.starline.resi.service.RabbitPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
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

    private final RabbitPublisher rabbitPublisher;
    private final MeterRegistry meterRegistry;


    @Override
    public ResiUpdateResult process(@NonNull Resi item) {
        try {

            var request = ScrappingRequestEvent.builder()
                    .trackingNumber(item.getTrackingNumber())
                    .courierCode(item.getCourier().getCode())
                    .phoneLast5(item.getAdditionalValue1())
                    .userId(item.getUserId())
                    .additionalValue1(item.getAdditionalValue1())
                    .type(ScrappingType.UPDATE)
                    .build();

            rabbitPublisher.publishScrappingRequest(request);
            meterRegistry.counter("resi.processing.processed").increment();
            log.info("Published update scrapping request for resi {}", item.getTrackingNumber());


        } catch (Exception e) {
            log.error("Error processing tracking number {}: {}",
                    item.getTrackingNumber(), e.getMessage());
            meterRegistry.counter("resi.processing.failed").increment();
            throw e; // Let Spring Batch handle retry logic
        }
        return ResiUpdateResult.builder()
                .processedAt(LocalDateTime.now())
                .trackingNumber(item.getTrackingNumber())
                .build();
    }

}

