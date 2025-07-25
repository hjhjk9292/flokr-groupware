package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.service.FacilityService;
import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.ReservationResponse;
import com.flokr.groupwarebackend.dto.ApprovalRequest;
import com.flokr.groupwarebackend.entity.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    private final FacilityService facilityService;

    /**
     * 모든 예약 목록 조회 (관리자용)
     */
    @GetMapping
    public ApiResponse<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> allReservations = facilityService.getAllReservationsForAdmin();
        return ApiResponse.success(allReservations);
    }

    /**
     * 승인 대기 예약 목록 조회
     */
    @GetMapping("/pending")
    public ApiResponse<List<ReservationResponse>> getPendingReservations() {
        List<ReservationResponse> pendingReservations = facilityService.getPendingReservations();
        return ApiResponse.success(pendingReservations);
    }

    /**
     * 예약 상태 변경 (통합)
     */
    @PutMapping("/{reservationNo}/status")
    public ApiResponse<ReservationResponse> updateReservationStatus(
            @PathVariable Long reservationNo,
            @RequestBody StatusUpdateRequest request) {

        ReservationResponse result = facilityService.updateReservationStatus(
                reservationNo, request.getStatus(), request.getComment());

        String message = request.getStatus() == ReservationStatus.APPROVED ?
                "예약이 승인되었습니다." : "예약이 거절되었습니다.";

        return ApiResponse.success(message, result);
    }

    /**
     * 예약 승인 (기존 API 호환성 유지)
     */
    @PutMapping("/{reservationNo}/approve")
    public ApiResponse<ReservationResponse> approveReservation(
            @PathVariable Long reservationNo,
            @RequestBody(required = false) ApprovalRequest request) {

        String comment = request != null ? request.getComment() : null;
        ReservationResponse result = facilityService.updateReservationStatus(
                reservationNo, ReservationStatus.APPROVED, comment);

        return ApiResponse.success("예약이 승인되었습니다.", result);
    }

    /**
     * 예약 거절 (기존 API 호환성 유지)
     */
    @PutMapping("/{reservationNo}/reject")
    public ApiResponse<ReservationResponse> rejectReservation(
            @PathVariable Long reservationNo,
            @RequestBody(required = false) ApprovalRequest request) {

        String comment = request != null ? request.getComment() : null;
        ReservationResponse result = facilityService.updateReservationStatus(
                reservationNo, ReservationStatus.CANCELED, comment);

        return ApiResponse.success("예약이 거부되었습니다.", result);
    }
}

// StatusUpdateRequest DTO
class StatusUpdateRequest {
    private ReservationStatus status;
    private String comment;

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}