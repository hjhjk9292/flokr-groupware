package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.service.FacilityService;
import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.FacilityResponse;
import com.flokr.groupwarebackend.dto.FacilityDetailResponse;
import com.flokr.groupwarebackend.dto.FacilityCreateRequest;
import com.flokr.groupwarebackend.dto.FacilityUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FacilityController {

    private final FacilityService facilityService;

    /**
     * 시설 목록 조회 (모든 사용자)
     */
    @GetMapping("/facilities")
    public ApiResponse<List<FacilityResponse>> getAllFacilities() {
        List<FacilityResponse> facilities = facilityService.getAllFacilities();
        return ApiResponse.success(facilities);
    }

    /**
     * 시설 상세 조회 (모든 사용자)
     */
    @GetMapping("/facilities/{facilityNo}")
    public ApiResponse<FacilityDetailResponse> getFacilityDetail(@PathVariable Long facilityNo) {
        FacilityDetailResponse detail = facilityService.getFacilityWithReservations(facilityNo);
        return ApiResponse.success(detail);
    }

    /**
     * 시설 추가 (관리자 전용)
     */
    @PostMapping("/facilities")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FacilityResponse> createFacility(@RequestBody @Valid FacilityCreateRequest request) {
        FacilityResponse facility = facilityService.createFacility(request);
        return ApiResponse.success("시설이 추가되었습니다.", facility);
    }

    /**
     * 시설 수정 (관리자 전용)
     */
    @PutMapping("/facilities/{facilityNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FacilityResponse> updateFacility(
            @PathVariable Long facilityNo,
            @RequestBody @Valid FacilityUpdateRequest request) {

        FacilityResponse facility = facilityService.updateFacility(facilityNo, request);
        return ApiResponse.success("시설이 수정되었습니다.", facility);
    }

    /**
     * 시설 삭제 (관리자 전용)
     */
    @DeleteMapping("/facilities/{facilityNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteFacility(@PathVariable Long facilityNo) {
        facilityService.deleteFacility(facilityNo);
        return ApiResponse.success("시설이 삭제되었습니다.");
    }
}