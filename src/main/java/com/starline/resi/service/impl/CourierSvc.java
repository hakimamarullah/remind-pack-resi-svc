package com.starline.resi.service.impl;

import com.starline.resi.annotations.CacheablePage;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.PageWrapper;
import com.starline.resi.dto.courier.CourierInfo;
import com.starline.resi.model.Courier;
import com.starline.resi.repository.CourierRepository;
import com.starline.resi.service.CourierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "courier")
@Slf4j
public class CourierSvc implements CourierService {

    private final CourierRepository courierRepository;

    @CacheablePage
    @Override
    public ApiResponse<PageWrapper<CourierInfo>> findAll(Pageable pageable) {
        var pageCourier = courierRepository.findAll(pageable)
                .map(this::toCourierInfo);
        return ApiResponse.setPagedResponse(pageCourier);
    }

    @CacheablePage
    @Override
    public ApiResponse<PageWrapper<CourierInfo>> findByName(String name, Pageable pageable) {
        var pageCourier = courierRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toCourierInfo);
        return ApiResponse.setPagedResponse(pageCourier);
    }

    private CourierInfo toCourierInfo(Courier c) {
        return CourierInfo.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .build();
    }
}
