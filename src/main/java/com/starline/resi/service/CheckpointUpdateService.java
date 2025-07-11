package com.starline.resi.service;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.resi.CheckpointUpdateResult;
import com.starline.resi.dto.projection.ResiProjection;

public interface CheckpointUpdateService {
    ApiResponse<CheckpointUpdateResult> updateCheckpoint(ResiProjection resi);
}
