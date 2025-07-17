package com.starline.resi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.rabbit.ScrappingRequestEvent;
import com.starline.resi.dto.rabbit.ScrappingResultEvent;
import com.starline.resi.dto.rabbit.enums.ScrappingType;
import com.starline.resi.dto.resi.AddResiRequest;
import com.starline.resi.dto.resi.ResiInfo;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.exceptions.DataNotFoundException;
import com.starline.resi.exceptions.DuplicateDataException;
import com.starline.resi.exceptions.TooManyActiveResiException;
import com.starline.resi.model.Courier;
import com.starline.resi.model.Resi;
import com.starline.resi.repository.CourierRepository;
import com.starline.resi.repository.ResiRepository;
import com.starline.resi.service.RabbitPublisher;
import com.starline.resi.service.ResiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "resi")
@Slf4j
public class ResiSvc implements ResiService {

    private final ResiRepository resiRepository;

    private final CourierRepository courierRepository;

    private final RabbitPublisher rabbitPublisher;

    private final ObjectMapper mapper;

    @Value("${max.resi.active:5}")
    private Integer maxResiActive;


    @Async
    @Transactional
    @CacheEvict(key = "#payload.userId")
    @Override
    public void addResiAsync(AddResiRequest payload) {
        if (resiRepository.countByUserId(payload.getUserId()) >= maxResiActive) {
            throw new TooManyActiveResiException();
        }

        if (resiRepository.countByTrackingNumberAndUserId(payload.getTrackingNumber(), payload.getUserId()) > 0) {
            throw new DuplicateDataException("Tracking number " + payload.getTrackingNumber() + " already exists for this user");
        }

        Courier courier = courierRepository.findById(payload.getCourierId())
                .orElseThrow(() -> new DataNotFoundException("Courier " + payload.getCourierId() + " not found"));

        var request = ScrappingRequestEvent.builder()
                .trackingNumber(payload.getTrackingNumber())
                .courierCode(courier.getCode())
                .phoneLast5(payload.getAdditionalValue1())
                .userId(payload.getUserId())
                .additionalValue1(payload.getAdditionalValue1())
                .type(ScrappingType.ADD)
                .build();


        rabbitPublisher.publishScrappingRequest(request);
        log.info("Published scrapping request for resi {}", payload.getTrackingNumber());

    }

    @Cacheable(key = "#userId")
    @Override
    public ApiResponse<List<ResiInfo>> getResiInfoByUserId(Long userId) {
        return ApiResponse.setSuccess(mapper.convertValue(resiRepository.getResiByUserId(userId), new TypeReference<>() {
        }));
    }

    @CacheEvict(key = "#userId")
    @Transactional
    @Modifying
    @Override
    public void deleteResiByUserIdAndTrackingNumber(Long userId, String trackingNumber) {
        resiRepository.deleteByTrackingNumberAndUserId(trackingNumber, userId);
    }

    @Transactional
    @CacheEvict(key = "#payload.userId")
    @Override
    public void handleScrappingResultEvent(ScrappingResultEvent payload) {
        log.info("Receive scrapping result for resi {}", payload.getTrackingNumber());
        Optional<Courier> courier = courierRepository.findByCode(payload.getCourierCode());
        if (courier.isEmpty()) {
            log.info("Courier {} not found", payload.getCourierCode());
            return;
        }
        Optional<Resi> existingResi = resiRepository.findByTrackingNumberAndUserId(payload.getTrackingNumber(), payload.getUserId());
        if (ScrappingType.UPDATE.equals(payload.getType())) {
            log.info("Update resi {}", payload.getTrackingNumber());
            ResiUpdateNotification notification = ResiUpdateNotification.builder()
                    .trackingNumber(payload.getTrackingNumber())
                    .userId(payload.getUserId())
                    .lastCheckpoint(payload.getCheckpoint())
                    .lastCheckpointTime(payload.getTimestamp())
                    .courierName(courier.get().getName())
                    .build();
            AtomicBoolean isSendNotification = new AtomicBoolean(false);
            existingResi
                    .ifPresent(resi -> {
                        isSendNotification.set(!Objects.equals(payload.getCheckpoint(), resi.getLastCheckpoint()));

                        notification.setPreviousCheckpoint(resi.getLastCheckpoint());
                        notification.setPreviousCheckpointTime(resi.getOriginalCheckpointTime());
                        resi.setLastCheckpoint(payload.getCheckpoint());
                        resi.setOriginalCheckpointTime(payload.getTimestamp());
                        resi.setLastCheckpointUpdate(LocalDateTime.now());
                        resiRepository.save(resi);


                    });

            if (Boolean.TRUE.equals(isSendNotification.get())) {
                rabbitPublisher.publishResiUpdateNotification(notification);
            }
            log.info("Successfully updated resi {}", payload.getTrackingNumber());
            return;
        }

        if (resiRepository.countByTrackingNumberAndUserId(payload.getTrackingNumber(), payload.getUserId()) > 0) {
            log.info("Resi {} already exists for this user", payload.getTrackingNumber());
            return;
        }

        if (resiRepository.countByUserId(payload.getUserId()) >= maxResiActive) {
            log.info("Max resi active reached for user {}", payload.getUserId());
            return;
        }


        Resi newResi = new Resi();
        newResi.setTrackingNumber(payload.getTrackingNumber());
        newResi.setUserId(payload.getUserId());
        newResi.setAdditionalValue1(payload.getAdditionalValue1());
        newResi.setLastCheckpoint(payload.getCheckpoint());
        newResi.setOriginalCheckpointTime(payload.getTimestamp());
        newResi.setCourier(courier.get());
        resiRepository.save(newResi);


        ResiUpdateNotification notification = ResiUpdateNotification.builder()
                .trackingNumber(payload.getTrackingNumber())
                .userId(payload.getUserId())
                .lastCheckpoint(payload.getCheckpoint())
                .lastCheckpointTime(payload.getTimestamp())
                .courierName(courier.get().getName())
                .build();

        log.info("Successfully added resi {}", payload.getTrackingNumber());
        rabbitPublisher.publishResiUpdateNotification(notification);
        log.info("Success Event sent {}", payload.getTrackingNumber());
    }
}
