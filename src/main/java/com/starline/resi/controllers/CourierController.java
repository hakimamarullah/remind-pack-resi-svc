package com.starline.resi.controllers;

import com.starline.resi.annotations.LogRequestResponse;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.PageWrapper;
import com.starline.resi.dto.courier.CourierInfo;
import com.starline.resi.service.CourierService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/couriers")
@LogRequestResponse
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get list of courier info or filter by name")
    public ResponseEntity<ApiResponse<PageWrapper<CourierInfo>>> findByNameOrAll(@RequestParam(required = false) String name, Pageable pageable) {
        if (!StringUtils.isBlank(name)) {
            return courierService.findByName(name, pageable).toResponseEntity();
        }
        return courierService.findAll(pageable).toResponseEntity();
    }
}
