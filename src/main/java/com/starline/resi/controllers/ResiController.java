package com.starline.resi.controllers;

import com.starline.resi.annotations.LogRequestResponse;
import com.starline.resi.dto.ApiResponse;
import com.starline.resi.dto.resi.AddResiRequest;
import com.starline.resi.dto.resi.ResiInfo;
import com.starline.resi.service.ResiService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/resi")
@LogRequestResponse
@RequiredArgsConstructor
public class ResiController {

    private final ResiService resiService;

    private final MessageSource messageSource;


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add new resi for user")
    public ResponseEntity<ApiResponse<Void>> addResi(@RequestBody @Valid AddResiRequest payload) {
        return resiService.addResi(payload).toResponseEntity();
    }

    @GetMapping(value = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get list of resi info by user id")
    public ResponseEntity<ApiResponse<List<ResiInfo>>> getResiInfoByUserId(@PathVariable Long userId) {
        return resiService.getResiInfoByUserId(userId).toResponseEntity();
    }

    @DeleteMapping(value = "/{trackingNumber}/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete resi by user id and tracking number")
    public ResponseEntity<ApiResponse<String>> deleteResiByUserIdAndTrackingNumber(@PathVariable Long userId, @PathVariable String trackingNumber) {
        resiService.deleteResiByUserIdAndTrackingNumber(userId, trackingNumber);
        return ApiResponse.setSuccess(messageSource.getMessage("resi.delete.message", null, LocaleContextHolder.getLocale())).toResponseEntity();
    }
}
