package com.starline.resi.service;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.rabbit.ScrappingResultEvent;
import com.starline.resi.dto.resi.AddResiRequest;
import com.starline.resi.dto.resi.ResiInfo;

import java.util.List;

public interface ResiService {

    ApiResponse<Void> addResi(AddResiRequest payload);

    ApiResponse<List<ResiInfo>> getResiInfoByUserId(Long userId);

    void deleteResiByUserIdAndTrackingNumber(Long userId, String trackingNumber);

    void handleScrappingResultEvent(ScrappingResultEvent payload);


    void handleScrappingUpdateEvent(ScrappingResultEvent payload);
}
