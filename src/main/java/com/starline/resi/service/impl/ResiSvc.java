package com.starline.resi.service.impl;

import com.starline.resi.dto.AddResiRequest;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.CekResiScrapResponse;
import com.starline.resi.dto.ResiUpdateNotification;
import com.starline.resi.dto.ScrappingRequest;
import com.starline.resi.dto.projection.ResiProjection;
import com.starline.resi.exceptions.DataNotFoundException;
import com.starline.resi.exceptions.DuplicateDataException;
import com.starline.resi.exceptions.TooManyActiveResiException;
import com.starline.resi.feign.ScrapperProxySvc;
import com.starline.resi.model.Courier;
import com.starline.resi.model.Resi;
import com.starline.resi.repository.CourierRepository;
import com.starline.resi.repository.ResiRepository;
import com.starline.resi.service.NotificationService;
import com.starline.resi.service.ResiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResiSvc implements ResiService {

    private final ResiRepository resiRepository;

    private final CourierRepository courierRepository;

    private final ScrapperProxySvc scrapperProxySvc;

    private final NotificationService notificationService;

    @Value("${max.resi.active:5}")
    private Integer maxResiActive;

    @Transactional
    @Override
    public ApiResponse<String> addResi(AddResiRequest payload) {
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
                .build();
        notificationService.sendCheckpointUpdateNotification(notification);
        return ApiResponse.setResponse("Resi added successfully", 201);
    }

    @Override
    public ApiResponse<List<ResiProjection>> getResiInfoByUserId(Long userId) {
        return ApiResponse.setSuccess(resiRepository.getResiByUserId(userId));
    }

    @Transactional
    @Modifying
    @Override
    public void deleteResiByUserIdAndTrackingNumber(Long userId, String trackingNumber) {
        resiRepository.deleteByTrackingNumberAndUserId(trackingNumber, userId);
    }
}
