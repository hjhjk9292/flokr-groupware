package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.service.FacilityService;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final EmployeeRepository employeeRepository;

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

    @GetMapping("/my-reservations")
    public ApiResponse<List<ReservationResponse>> getMyReservations(HttpServletRequest request) {
        Long empNo = getCurrentEmployeeNo(request);
        List<ReservationResponse> reservations = facilityService.getMyReservations(empNo);
        return ApiResponse.success(reservations);
    }

    @GetMapping("/reservations/{reservationNo}")
    public ApiResponse<ReservationResponse> getReservationDetail(
            @PathVariable Long reservationNo,
            HttpServletRequest request) {

        Long empNo = getCurrentEmployeeNo(request);
        ReservationResponse reservation = facilityService.getReservationDetail(reservationNo, empNo);
        return ApiResponse.success(reservation);
    }

    @PutMapping("/reservations/{reservationNo}")
    public ApiResponse<ReservationResponse> updateMyReservation(
            @PathVariable Long reservationNo,
            @RequestBody @Valid ReservationRequest request,
            HttpServletRequest httpRequest) {

        Long empNo = getCurrentEmployeeNo(httpRequest);
        ReservationResponse reservation = facilityService.updateMyReservation(reservationNo, request, empNo);
        return ApiResponse.success("예약이 수정되었습니다.", reservation);
    }

    @PutMapping("/reservations/{reservationNo}/cancel")
    public ApiResponse<ReservationResponse> cancelReservation(
            @PathVariable Long reservationNo,
            HttpServletRequest request) {

        Long empNo = getCurrentEmployeeNo(request);
        ReservationResponse reservation = facilityService.cancelReservation(reservationNo, empNo);
        return ApiResponse.success("예약이 취소되었습니다.", reservation);
    }

    @GetMapping("/admin/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> allReservations = facilityService.getAllReservationsForAdmin();
        return ApiResponse.success(allReservations);
    }

    @GetMapping("/admin/reservations/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ReservationResponse>> getPendingReservations() {
        List<ReservationResponse> pendingReservations = facilityService.getPendingReservations();
        return ApiResponse.success(pendingReservations);
    }

    @PutMapping("/admin/reservations/{reservationNo}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReservationResponse> updateReservationStatus(
            @PathVariable Long reservationNo,
            @RequestBody StatusUpdateRequest request) {

        log.info("예약 상태 변경 요청 - 예약번호: {}, 상태: {}", reservationNo, request.getStatus());

        try {
            ReservationResponse result = facilityService.updateReservationStatus(
                    reservationNo, request.getStatus(), request.getComment());

            String message = request.getStatus() == ReservationStatus.APPROVED ?
                    "예약이 승인되었습니다." : "예약이 거절되었습니다.";

            log.info("예약 상태 변경 완료 - 예약번호: {}", reservationNo);
            return ApiResponse.success(message, result);

        } catch (Exception e) {
            log.error("예약 상태 변경 중 오류 발생 - 예약번호: {}", reservationNo, e);
            String message = request.getStatus() == ReservationStatus.APPROVED ?
                    "예약 승인 중 오류가 발생했습니다." : "예약 거절 중 오류가 발생했습니다.";
            return ApiResponse.error(message);
        }
    }

    @PutMapping("/admin/reservations/{reservationNo}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReservationResponse> approveReservation(
            @PathVariable Long reservationNo,
            @RequestBody(required = false) ApprovalRequest request) {

        try {
            String comment = request != null ? request.getComment() : null;
            ReservationResponse result = facilityService.updateReservationStatus(
                    reservationNo, ReservationStatus.APPROVED, comment);

            return ApiResponse.success("예약이 승인되었습니다.", result);
        } catch (Exception e) {
            log.error("예약 승인 중 오류 발생", e);
            return ApiResponse.error("예약 승인 중 오류가 발생했습니다.");
        }
    }

    @PutMapping("/admin/reservations/{reservationNo}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReservationResponse> rejectReservation(
            @PathVariable Long reservationNo,
            @RequestBody(required = false) ApprovalRequest request) {

        try {
            String comment = request != null ? request.getComment() : null;
            ReservationResponse result = facilityService.updateReservationStatus(
                    reservationNo, ReservationStatus.CANCELED, comment);

            return ApiResponse.success("예약이 거절되었습니다.", result);
        } catch (Exception e) {
            log.error("예약 거절 중 오류 발생", e);
            return ApiResponse.error("예약 거절 중 오류가 발생했습니다.");
        }
    }

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

class StatusUpdateRequest {
    private ReservationStatus status;
    private String comment;

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}