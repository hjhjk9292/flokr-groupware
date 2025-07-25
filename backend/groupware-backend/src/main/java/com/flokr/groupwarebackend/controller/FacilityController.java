package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.service.FacilityService;
import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.FacilityResponse;
import com.flokr.groupwarebackend.dto.FacilityDetailResponse;
import com.flokr.groupwarebackend.dto.ReservationRequest;
import com.flokr.groupwarebackend.dto.ReservationResponse;
import com.flokr.groupwarebackend.dto.FacilityCreateRequest;
import com.flokr.groupwarebackend.dto.FacilityUpdateRequest;
import com.flokr.groupwarebackend.security.JwtTokenProvider;
import com.flokr.groupwarebackend.repository.EmployeeRepository;
import com.flokr.groupwarebackend.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FacilityController {

    private final FacilityService facilityService;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeRepository employeeRepository;

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
     * 예약 생성
     */
    @PostMapping("/facilities/{facilityNo}/reservations")
    public ApiResponse<ReservationResponse> createReservation(
            @PathVariable Long facilityNo,
            @RequestBody @Valid ReservationRequest request,
            HttpServletRequest httpRequest) {

        Long empNo = getCurrentEmployeeNo(httpRequest);
        request.setFacilityNo(facilityNo);

        ReservationResponse reservation = facilityService.createReservation(request, empNo);
        return ApiResponse.success("예약이 신청되었습니다.", reservation);
    }

    /**
     * 내 예약 목록 조회
     */
    @GetMapping("/my-reservations")
    public ApiResponse<List<ReservationResponse>> getMyReservations(HttpServletRequest request) {
        Long empNo = getCurrentEmployeeNo(request);
        List<ReservationResponse> reservations = facilityService.getMyReservations(empNo);
        return ApiResponse.success(reservations);
    }

    /**
     * 예약 취소
     */
    @PutMapping("/reservations/{reservationNo}/cancel")
    public ApiResponse<ReservationResponse> cancelReservation(
            @PathVariable Long reservationNo,
            HttpServletRequest request) {

        Long empNo = getCurrentEmployeeNo(request);
        ReservationResponse reservation = facilityService.cancelReservation(reservationNo, empNo);
        return ApiResponse.success("예약이 취소되었습니다.", reservation);
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

    /**
     * JWT에서 직원 번호 추출
     */
    private Long getCurrentEmployeeNo(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                String empId = jwtTokenProvider.getUserId(token);
                Employee employee = employeeRepository.findByEmpId(empId)
                        .orElseThrow(() -> new RuntimeException("직원 정보를 찾을 수 없습니다: " + empId));
                return employee.getEmpNo();
            } catch (Exception e) {
                throw new RuntimeException("JWT 토큰에서 사용자 정보를 추출할 수 없습니다: " + e.getMessage());
            }
        }
        throw new RuntimeException("JWT 토큰이 없습니다.");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}