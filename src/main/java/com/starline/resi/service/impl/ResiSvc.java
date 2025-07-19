package com.starline.resi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.proxy.subscriptions.HasActiveSubscription;
import com.starline.resi.dto.proxy.subscriptions.SubscriptionInfo;
import com.starline.resi.dto.proxy.subscriptions.SubscriptionStatus;
import com.starline.resi.dto.rabbit.ScrappingRequestEvent;
import com.starline.resi.dto.rabbit.ScrappingResultEvent;
import com.starline.resi.dto.rabbit.enums.ScrappingType;
import com.starline.resi.dto.resi.AddResiRequest;
import com.starline.resi.dto.resi.ResiInfo;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.exceptions.DataNotFoundException;
import com.starline.resi.exceptions.DuplicateDataException;
import com.starline.resi.exceptions.TooManyActiveResiException;
import com.starline.resi.feign.SubscriptionProxySvc;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "resi")
@Slf4j
public class ResiSvc implements ResiService {

    public static final String COURIER_NOT_FOUND_LOG = "Courier {} not found";
    private final ResiRepository resiRepository;

    private final CourierRepository courierRepository;

    private final RabbitPublisher rabbitPublisher;

    private final SubscriptionProxySvc subscriptionProxySvc;

    private final ObjectMapper mapper;

    @Value("${max.resi.active:5}")
    private Integer maxResiActive;


    @Transactional
    @CacheEvict(key = "#payload.userId")
    @Override
    public ApiResponse<Void> addResi(AddResiRequest payload) {
        if (resiRepository.countByUserId(payload.getUserId()) >= maxResiActive) {
            throw new TooManyActiveResiException();
        }

        if (resiRepository.countByTrackingNumberAndUserId(payload.getTrackingNumber(), payload.getUserId()) > 0) {
            throw new DuplicateDataException("Tracking number " + payload.getTrackingNumber() + " already exists for this user");
        }

        ApiResponse<HasActiveSubscription> checkHasActiveSubscription = subscriptionProxySvc.checkHasActiveSubscription(payload.getUserId());
        boolean hasActiveSubscription = Optional.ofNullable(checkHasActiveSubscription.getData())
                .map(HasActiveSubscription::isActiveSubscription)
                .orElse(false);
        if (!hasActiveSubscription) {
            return ApiResponse.setResponse(null, "You don't have an active subscription", 400);
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

        return ApiResponse.setResponse(null, "Your AWB is being processed!", 201);

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
            log.info(COURIER_NOT_FOUND_LOG, payload.getCourierCode());
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


        ApiResponse<List<SubscriptionInfo>> subscriptionInfosResponse = subscriptionProxySvc.getSubscriptionInfoByUserId(payload.getUserId());

        List<SubscriptionInfo> subscriptionInfos = subscriptionInfosResponse.getData();
        LocalDate subscriptionExpiryDate = subscriptionInfos.stream()
                .filter( it -> Objects.equals(it.getStatus(), SubscriptionStatus.ACTIVE))
                .findFirst()
                .map(SubscriptionInfo::getExpiryDate)
                .orElse(null);

        if (Objects.isNull(subscriptionExpiryDate)) {
            log.info("User {} has no active subscription", payload.getUserId());
            return;
        }
        Resi newResi = new Resi();
        newResi.setTrackingNumber(payload.getTrackingNumber());
        newResi.setUserId(payload.getUserId());
        newResi.setAdditionalValue1(payload.getAdditionalValue1());
        newResi.setLastCheckpoint(payload.getCheckpoint());
        newResi.setOriginalCheckpointTime(payload.getTimestamp());
        newResi.setSubscriptionExpiryDate(subscriptionExpiryDate);
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

    @Transactional
    @CacheEvict(key = "#payload.userId")
    @Override
    public void handleScrappingUpdateEvent(ScrappingResultEvent payload) {
        log.info("Receive scrapping result to update AWB -> {}", payload.getTrackingNumber());
        Optional<Courier> courier = courierRepository.findByCode(payload.getCourierCode());
        Optional<Resi> existingResi = resiRepository.findByTrackingNumberAndUserId(payload.getTrackingNumber(), payload.getUserId());
        if (existingResi.isEmpty()) {
            log.info("Resi {} not found for this user", payload.getTrackingNumber());
            return;
        }

        String courierName = courier.map(Courier::getName).orElse(null);
        ResiUpdateNotification notification = ResiUpdateNotification.builder()
                .trackingNumber(payload.getTrackingNumber())
                .userId(payload.getUserId())
                .lastCheckpoint(payload.getCheckpoint())
                .lastCheckpointTime(payload.getTimestamp())
                .courierName(courierName)
                .build();

        boolean isSendNotification;

        var resi = existingResi.get();
        isSendNotification = !Objects.equals(payload.getCheckpoint(), resi.getLastCheckpoint());

        notification.setPreviousCheckpoint(resi.getLastCheckpoint());
        notification.setPreviousCheckpointTime(resi.getOriginalCheckpointTime());
        resi.setLastCheckpoint(payload.getCheckpoint());
        resi.setOriginalCheckpointTime(payload.getTimestamp());
        resi.setLastCheckpointUpdate(LocalDateTime.now());
        resiRepository.save(resi);

        if (Boolean.TRUE.equals(isSendNotification)) {
            rabbitPublisher.publishResiUpdateNotification(notification);
        }
        log.info("Successfully update resi {}", payload.getTrackingNumber());
    }
}
