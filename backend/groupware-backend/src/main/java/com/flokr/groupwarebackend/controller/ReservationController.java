// ReservationController.java - 알림 연동된 버전
package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.service.FacilityService;
import com.flokr.groupwarebackend.service.NotificationService;
import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.ReservationRequest;
import com.flokr.groupwarebackend.dto.ReservationResponse;
import com.flokr.groupwarebackend.dto.ApprovalRequest;
import com.flokr.groupwarebackend.entity.ReservationStatus;
import com.flokr.groupwarebackend.security.JwtTokenProvider;
import com.flokr.groupwarebackend.repository.EmployeeRepository;
import com.flokr.groupwarebackend.entity.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    private final FacilityService facilityService;
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeRepository employeeRepository;

    // ==================== 사용자 예약 기능 ====================

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
     * 예약 상세 조회
     */
    @GetMapping("/reservations/{reservationNo}")
    public ApiResponse<ReservationResponse> getReservationDetail(
            @PathVariable Long reservationNo,
            HttpServletRequest request) {

        Long empNo = getCurrentEmployeeNo(request);
        ReservationResponse reservation = facilityService.getReservationDetail(reservationNo, empNo);
        return ApiResponse.success(reservation);
    }

    /**
     * 예약 수정
     */
    @PutMapping("/reservations/{reservationNo}")
    public ApiResponse<ReservationResponse> updateMyReservation(
            @PathVariable Long reservationNo,
            @RequestBody @Valid ReservationRequest request,
            HttpServletRequest httpRequest) {

        Long empNo = getCurrentEmployeeNo(httpRequest);
        ReservationResponse reservation = facilityService.updateMyReservation(reservationNo, request, empNo);
        return ApiResponse.success("예약이 수정되었습니다.", reservation);
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

    // ==================== 관리자 예약 관리 기능 ====================

    /**
     * 모든 예약 목록 조회 (관리자용)
     */
    @GetMapping("/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> allReservations = facilityService.getAllReservationsForAdmin();
        return ApiResponse.success(allReservations);
    }

    /**
     * 승인 대기 예약 목록 조회
     */
    @GetMapping("/admin/reservations/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReservationResponse>> getPendingReservations() {
        List<ReservationResponse> pendingReservations = facilityService.getPendingReservations();
        return ApiResponse.success(pendingReservations);
    }

    /**
     * 예약 상태 변경 (통합) - 알림 발송 포함
     */
    @PutMapping("/admin/reservations/{reservationNo}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReservationResponse> updateReservationStatus(
            @PathVariable Long reservationNo,
            @RequestBody StatusUpdateRequest request) {

        try {
            // 예약 상태 업데이트
            ReservationResponse result = facilityService.updateReservationStatus(
                    reservationNo, request.getStatus(), request.getComment());

            // 알림 발송
            if (result != null && result.getEmpNo() != null) {
                String facilityName = result.getFacilityName();

                if (request.getStatus() == ReservationStatus.APPROVED) {
                    notificationService.sendFacilityApprovedNotification(result.getEmpNo(), facilityName);
                    log.info("시설 예약 승인 알림 발송: empNo={}, facilityName={}", result.getEmpNo(), facilityName);
                } else if (request.getStatus() == ReservationStatus.CANCELED) {
                    notificationService.sendFacilityRejectedNotification(result.getEmpNo(), facilityName);
                    log.info("시설 예약 거절 알림 발송: empNo={}, facilityName={}", result.getEmpNo(), facilityName);
                }
            }

            String message = request.getStatus() == ReservationStatus.APPROVED ?
                    "예약이 승인되었으며 알림이 발송되었습니다." : "예약이 거절되었으며 알림이 발송되었습니다.";

            return ApiResponse.success(message, result);
        } catch (Exception e) {
            log.error("예약 상태 변경 중 오류 발생", e);
            String message = request.getStatus() == ReservationStatus.APPROVED ?
                    "예약 승인 중 오류가 발생했습니다." : "예약 거절 중 오류가 발생했습니다.";
            return ApiResponse.error(message);
        }
    }

    /**
     * 예약 승인 - 알림 발송 포함
     */
    @PutMapping("/admin/reservations/{reservationNo}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReservationResponse> approveReservation(
            @PathVariable Long reservationNo,
            @RequestBody(required = false) ApprovalRequest request) {

        try {
            String comment = request != null ? request.getComment() : null;
            ReservationResponse result = facilityService.updateReservationStatus(
                    reservationNo, ReservationStatus.APPROVED, comment);

            // 승인 알림 발송
            if (result != null && result.getEmpNo() != null) {
                notificationService.sendFacilityApprovedNotification(
                        result.getEmpNo(), result.getFacilityName());
                log.info("시설 예약 승인 알림 발송: empNo={}, facilityName={}",
                        result.getEmpNo(), result.getFacilityName());
            }

            return ApiResponse.success("예약이 승인되었으며 알림이 발송되었습니다.", result);
        } catch (Exception e) {
            log.error("예약 승인 중 오류 발생", e);
            return ApiResponse.error("예약 승인 중 오류가 발생했습니다.");
        }
    }

    /**
     * 예약 거절 - 알림 발송 포함
     */
    @PutMapping("/admin/reservations/{reservationNo}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReservationResponse> rejectReservation(
            @PathVariable Long reservationNo,
            @RequestBody(required = false) ApprovalRequest request) {

        try {
            String comment = request != null ? request.getComment() : null;
            ReservationResponse result = facilityService.updateReservationStatus(
                    reservationNo, ReservationStatus.CANCELED, comment);

            // 거절 알림 발송
            if (result != null && result.getEmpNo() != null) {
                notificationService.sendFacilityRejectedNotification(
                        result.getEmpNo(), result.getFacilityName());
                log.info("시설 예약 거절 알림 발송: empNo={}, facilityName={}",
                        result.getEmpNo(), result.getFacilityName());
            }

            return ApiResponse.success("예약이 거부되었으며 알림이 발송되었습니다.", result);
        } catch (Exception e) {
            log.error("예약 거절 중 오류 발생", e);
            return ApiResponse.error("예약 거절 중 오류가 발생했습니다.");
        }
    }

    // ==================== 유틸리티 메서드 ====================

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

// StatusUpdateRequest DTO
class StatusUpdateRequest {
    private ReservationStatus status;
    private String comment;

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}