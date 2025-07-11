package com.starline.resi.service;

import com.starline.resi.dto.resi.AddResiRequest;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.projection.ResiProjection;

import java.util.List;

public interface ResiService {

    ApiResponse<String> addResi(AddResiRequest payload);

    ApiResponse<List<ResiProjection>> getResiInfoByUserId(Long userId);

    void deleteResiByUserIdAndTrackingNumber(Long userId, String trackingNumber);
}
