package com.starline.resi.service;

import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.PageWrapper;
import com.starline.resi.dto.courier.CourierInfo;
import org.springframework.data.domain.Pageable;

public interface CourierService {

    ApiResponse<PageWrapper<CourierInfo>> findAll(Pageable pageable);

    ApiResponse<PageWrapper<CourierInfo>> findByName(String name, Pageable pageable);
}
