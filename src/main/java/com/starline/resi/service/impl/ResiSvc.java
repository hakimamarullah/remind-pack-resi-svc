package com.starline.resi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.proxy.CekResiScrapResponse;
import com.starline.resi.dto.proxy.ScrappingRequest;
import com.starline.resi.dto.resi.AddResiRequest;
import com.starline.resi.dto.resi.ResiInfo;
import com.starline.resi.dto.resi.ResiUpdateNotification;
import com.starline.resi.exceptions.DataNotFoundException;
import com.starline.resi.exceptions.DuplicateDataException;
import com.starline.resi.exceptions.TooManyActiveResiException;
import com.starline.resi.feign.ScrapperProxySvc;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "resi")
@Slf4j
public class ResiSvc implements ResiService {

    private final ResiRepository resiRepository;

    private final CourierRepository courierRepository;

    private final ScrapperProxySvc scrapperProxySvc;

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

        var request = ScrappingRequest.builder()
                .trackingNumber(payload.getTrackingNumber())
                .courierCode(courier.getCode())
                .phoneLast5(payload.getAdditionalValue1())
                .build();
        ApiResponse<CekResiScrapResponse> response = scrapperProxySvc.scrap(request);

        var responseData = response.getData();
        Resi newResi = new Resi();
        newResi.setTrackingNumber(payload.getTrackingNumber());
        newResi.setUserId(payload.getUserId());
        newResi.setAdditionalValue1(payload.getAdditionalValue1());
        newResi.setLastCheckpoint(responseData.getCheckpoint());
        newResi.setOriginalCheckpointTime(responseData.getTimestamp());
        newResi.setCourier(courier);
        resiRepository.save(newResi);


        ResiUpdateNotification notification = ResiUpdateNotification.builder()
                .trackingNumber(payload.getTrackingNumber())
                .userId(payload.getUserId())
                .lastCheckpoint(responseData.getCheckpoint())
                .lastCheckpointTime(responseData.getTimestamp())
                .courierName(courier.getName())
                .build();
        rabbitPublisher.publishSuccessAddResi(notification);

        log.info("Successfully added resi {}", payload.getTrackingNumber());

    }

    @Cacheable(key = "#userId")
    @Override
    public ApiResponse<List<ResiInfo>> getResiInfoByUserId(Long userId) {
        return ApiResponse.setSuccess(mapper.convertValue(resiRepository.getResiByUserId(userId), new TypeReference<>() {}));
    }

    @CacheEvict(key = "#userId")
    @Transactional
    @Modifying
    @Override
    public void deleteResiByUserIdAndTrackingNumber(Long userId, String trackingNumber) {
        resiRepository.deleteByTrackingNumberAndUserId(trackingNumber, userId);
    }
}
