package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.entity.*;
import com.flokr.groupwarebackend.repository.FacilityRepository;
import com.flokr.groupwarebackend.repository.FacilityReservationRepository;
import com.flokr.groupwarebackend.repository.EmployeeRepository;
import com.flokr.groupwarebackend.repository.NotificationRepository;
import com.flokr.groupwarebackend.dto.*;
import com.flokr.groupwarebackend.exception.EntityNotFoundException;
import com.flokr.groupwarebackend.exception.ReservationConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilityReservationRepository reservationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities() {
        return facilityRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacilityDetailResponse getFacilityWithReservations(Long facilityNo) {
        Facility facility = facilityRepository.findById(facilityNo)
                .orElseThrow(() -> new EntityNotFoundException("시설을 찾을 수 없습니다. ID: " + facilityNo));

        List<FacilityReservation> reservations =
                reservationRepository.findByFacilityFacilityNoOrderByStartTimeAsc(facilityNo);

        FacilityDetailResponse response = new FacilityDetailResponse();
        response.setFacility(convertToResponse(facility));
        response.setReservations(reservations.stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList()));

        return response;
    }

    public ReservationResponse createReservation(ReservationRequest request, Long reserverEmpNo) {
        validateReservationRequest(request);

        Facility facility = facilityRepository.findById(request.getFacilityNo())
                .orElseThrow(() -> new EntityNotFoundException("시설을 찾을 수 없습니다."));

        Employee reserver = employeeRepository.findById(reserverEmpNo)
                .orElseThrow(() -> new EntityNotFoundException("직원 정보를 찾을 수 없습니다."));

        List<FacilityReservation> conflicts = reservationRepository
                .findConflictingReservations(request.getFacilityNo(),
                        request.getStartTime(), request.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new ReservationConflictException("해당 시간에 이미 예약이 있습니다.");
        }

        FacilityReservation reservation = new FacilityReservation(
                facility, reserver, request.getStartTime(),
                request.getEndTime(), request.getPurpose()
        );

        FacilityReservation saved = reservationRepository.save(reservation);
        sendReservationRequestNotification(saved);

        return convertToReservationResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationDetail(Long reservationNo, Long empNo) {
        FacilityReservation reservation = reservationRepository.findById(reservationNo)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다. ID: " + reservationNo));

        if (!reservation.getReserverEmpNo().equals(empNo)) {
            Employee employee = employeeRepository.findById(empNo)
                    .orElseThrow(() -> new EntityNotFoundException("직원 정보를 찾을 수 없습니다."));

            if (!"Y".equals(employee.getIsAdmin())) {
                throw new IllegalArgumentException("본인의 예약만 조회할 수 있습니다.");
            }
        }

        return convertToReservationResponse(reservation);
    }

    public ReservationResponse updateMyReservation(Long reservationNo, ReservationRequest request, Long empNo) {
        validateReservationRequest(request);

        FacilityReservation reservation = reservationRepository.findById(reservationNo)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다. ID: " + reservationNo));

        if (!reservation.getReserverEmpNo().equals(empNo)) {
            throw new IllegalArgumentException("본인의 예약만 수정할 수 있습니다.");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            if (reservation.getStatus() == ReservationStatus.APPROVED) {
                throw new IllegalArgumentException("승인된 예약은 수정할 수 없습니다. 취소 후 새로 예약해주세요.");
            } else if (reservation.getStatus() == ReservationStatus.CANCELED) {
                throw new IllegalArgumentException("취소된 예약은 수정할 수 없습니다.");
            } else {
                throw new IllegalArgumentException("현재 상태에서는 예약을 수정할 수 없습니다.");
            }
        }

        List<FacilityReservation> conflicts = reservationRepository
                .findConflictingReservationsExcluding(request.getFacilityNo(),
                        request.getStartTime(), request.getEndTime(), reservationNo);

        if (!conflicts.isEmpty()) {
            throw new ReservationConflictException("해당 시간에 이미 다른 예약이 있습니다.");
        }

        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPurpose(request.getPurpose());

        FacilityReservation updated = reservationRepository.save(reservation);
        sendReservationUpdateNotification(updated);

        return convertToReservationResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReservationResponse updateReservationStatus(Long reservationNo,
                                                       ReservationStatus status, String comment) {

        log.info("예약 상태 변경 시작 - 예약번호: {}, 상태: {}", reservationNo, status);

        FacilityReservation reservation = reservationRepository.findById(reservationNo)
                .orElseThrow(() -> {
                    log.error("예약을 찾을 수 없습니다. ID: {}", reservationNo);
                    return new EntityNotFoundException("예약을 찾을 수 없습니다. ID: " + reservationNo);
                });

        log.info("예약 정보 조회 성공: 기존상태={}, 예약자={}",
                reservation.getStatus(), reservation.getReserverEmpNo());

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(status);

        FacilityReservation updated = reservationRepository.save(reservation);
        log.info("예약 상태 DB 저장 완료: {} -> {}", oldStatus, status);

        if (oldStatus != status) {
            log.info("상태 변경 감지 - 즉시 알림 전송");
            sendReservationStatusUpdateNotification(updated, comment);
        }

        return convertToReservationResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(Long empNo) {
        return reservationRepository.findByReserverEmpNoOrderByCreateDateDesc(empNo)
                .stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ReservationResponse> getPendingReservations() {
        return reservationRepository.findByStatusOrderByCreateDateDesc(ReservationStatus.PENDING)
                .stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservationsForAdmin() {
        return reservationRepository.findRecentReservations()
                .stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse cancelReservation(Long reservationNo, Long empNo) {
        FacilityReservation reservation = reservationRepository.findById(reservationNo)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

        if (!reservation.getReserverEmpNo().equals(empNo)) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalArgumentException("이미 취소된 예약입니다.");
        }

        reservation.setStatus(ReservationStatus.CANCELED);
        FacilityReservation updated = reservationRepository.save(reservation);

        sendReservationCancelNotification(updated);

        return convertToReservationResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public FacilityResponse createFacility(FacilityCreateRequest request) {
        List<Facility> existingFacilities = facilityRepository.findByFacilityNameContaining(request.getFacilityName());
        boolean nameExists = existingFacilities.stream()
                .anyMatch(f -> f.getFacilityName().equals(request.getFacilityName()));

        if (nameExists) {
            throw new IllegalArgumentException("이미 존재하는 시설명입니다.");
        }

        Facility facility = new Facility();
        facility.setFacilityName(request.getFacilityName());
        facility.setFacilityLocation(request.getFacilityLocation());
        facility.setCapacity(request.getCapacity());
        facility.setDescription(request.getDescription());

        Facility saved = facilityRepository.save(facility);
        log.info("새 시설 추가됨: {}", saved.getFacilityName());

        return convertToResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public FacilityResponse updateFacility(Long facilityNo, FacilityUpdateRequest request) {
        Facility facility = facilityRepository.findById(facilityNo)
                .orElseThrow(() -> new EntityNotFoundException("시설을 찾을 수 없습니다. ID: " + facilityNo));

        if (!facility.getFacilityName().equals(request.getFacilityName())) {
            List<Facility> existingFacilities = facilityRepository.findByFacilityNameContaining(request.getFacilityName());
            boolean nameExists = existingFacilities.stream()
                    .anyMatch(f -> f.getFacilityName().equals(request.getFacilityName()));
            if (nameExists) {
                throw new IllegalArgumentException("이미 존재하는 시설명입니다.");
            }
        }

        facility.setFacilityName(request.getFacilityName());
        facility.setFacilityLocation(request.getFacilityLocation());
        facility.setCapacity(request.getCapacity());
        facility.setDescription(request.getDescription());

        Facility updated = facilityRepository.save(facility);
        log.info("시설 수정됨: {}", updated.getFacilityName());

        return convertToResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFacility(Long facilityNo) {
        Facility facility = facilityRepository.findById(facilityNo)
                .orElseThrow(() -> new EntityNotFoundException("시설을 찾을 수 없습니다. ID: " + facilityNo));

        List<FacilityReservation> activeReservations = reservationRepository
                .findByFacilityFacilityNoOrderByStartTimeAsc(facilityNo)
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.APPROVED)
                .filter(r -> r.getStartTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        if (!activeReservations.isEmpty()) {
            throw new IllegalArgumentException("활성 예약이 있는 시설은 삭제할 수 없습니다.");
        }

        String facilityName = facility.getFacilityName();
        facilityRepository.delete(facility);
        log.info("시설 삭제됨: {}", facilityName);
    }

    private void sendReservationRequestNotification(FacilityReservation reservation) {
        try {
            List<Employee> admins = employeeRepository.findByIsAdmin("Y");

            String title = "새 시설 예약 요청";
            String content = String.format("%s님이 %s를 %s에 예약 요청했습니다.",
                    reservation.getReserverName(),
                    reservation.getFacilityName(),
                    reservation.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));

            for (Employee admin : admins) {
                try {
                    Notification notification = new Notification(
                            admin.getEmpNo(),
                            "FACILITY",
                            title,
                            content,
                            "FACILITY_RESERVATION",
                            reservation.getReservationNo().toString(),
                            "HIGH"
                    );

                    notificationRepository.save(notification);
                    sendWebSocketNotification(admin, notification);

                    log.info("알림 저장 및 전송 완료 - 관리자: {}, 예약: {}",
                            admin.getEmpNo(), reservation.getReservationNo());
                } catch (Exception e) {
                    log.error("관리자 {}에게 알림 전송 실패", admin.getEmpNo(), e);
                }
            }
        } catch (Exception e) {
            log.error("예약 요청 알림 전송 실패", e);
        }
    }

    private void sendReservationStatusUpdateNotification(FacilityReservation reservation, String comment) {
        try {
            log.info("예약 상태 변경 알림 전송 시작 - 예약번호: {}, 상태: {}",
                    reservation.getReservationNo(), reservation.getStatus());

            Employee reserver = employeeRepository.findById(reservation.getReserverEmpNo())
                    .orElse(null);

            if (reserver == null) {
                log.error("예약자 정보를 찾을 수 없습니다. empNo: {}", reservation.getReserverEmpNo());
                return;
            }

            log.info("예약자 정보 조회 성공: empId={}, empName={}", reserver.getEmpId(), reserver.getEmpName());

            String statusText = getStatusText(reservation.getStatus());
            String notificationType = reservation.getStatus() == ReservationStatus.APPROVED ?
                    "FACILITY_APPROVED" : "FACILITY_REJECTED";

            String title = "시설 예약 " + statusText;
            String content = String.format("시설 예약이 %s되었습니다. 시설: %s 일시: %s",
                    statusText,
                    reservation.getFacilityName(),
                    reservation.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));

            if (comment != null && !comment.trim().isEmpty()) {
                content += " 사유: " + comment;
            }

            log.info("알림 내용 생성 완료 - 제목: {}, 타입: {}", title, notificationType);

            try {
                Notification notification = new Notification(
                        reservation.getReserverEmpNo(),
                        "FACILITY",
                        title,
                        content,
                        "FACILITY_RESERVATION",
                        reservation.getReservationNo().toString(),
                        reservation.getStatus() == ReservationStatus.APPROVED ? "NORMAL" : "HIGH"
                );

                Notification savedNotification = notificationRepository.save(notification);
                log.info("알림 DB 저장 완료 - 알림번호: {}", savedNotification.getNotificationNo());

                sendWebSocketNotificationWithType(reserver, savedNotification, notificationType);
            } catch (Exception e) {
                log.error("알림 생성/저장 실패 - 예약번호: {}", reservation.getReservationNo(), e);
            }

        } catch (Exception e) {
            log.error("예약 상태 변경 알림 전송 실패 - 예약번호: {}", reservation.getReservationNo(), e);
        }
    }

    private void sendReservationUpdateNotification(FacilityReservation reservation) {
        try {
            List<Employee> admins = employeeRepository.findByIsAdmin("Y");

            String title = "시설 예약 수정";
            String content = String.format("%s님이 %s 예약을 수정했습니다. 일시: %s",
                    reservation.getReserverName(),
                    reservation.getFacilityName(),
                    reservation.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));

            for (Employee admin : admins) {
                try {
                    Notification notification = new Notification(
                            admin.getEmpNo(),
                            "FACILITY",
                            title,
                            content,
                            "FACILITY_RESERVATION",
                            reservation.getReservationNo().toString(),
                            "NORMAL"
                    );

                    notificationRepository.save(notification);
                    sendWebSocketNotification(admin, notification);

                    log.info("예약 수정 알림 저장 및 전송 완료 - 관리자: {}, 예약: {}",
                            admin.getEmpNo(), reservation.getReservationNo());
                } catch (Exception e) {
                    log.error("관리자 {}에게 수정 알림 전송 실패", admin.getEmpNo(), e);
                }
            }
        } catch (Exception e) {
            log.error("예약 수정 알림 전송 실패", e);
        }
    }

    private void sendReservationCancelNotification(FacilityReservation reservation) {
        try {
            List<Employee> admins = employeeRepository.findByIsAdmin("Y");

            String title = "시설 예약 취소";
            String content = String.format("%s님이 %s 예약을 취소했습니다. 일시: %s",
                    reservation.getReserverName(),
                    reservation.getFacilityName(),
                    reservation.getStartTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));

            for (Employee admin : admins) {
                try {
                    Notification notification = new Notification(
                            admin.getEmpNo(),
                            "FACILITY",
                            title,
                            content,
                            "FACILITY_RESERVATION",
                            reservation.getReservationNo().toString(),
                            "NORMAL"
                    );

                    notificationRepository.save(notification);
                    sendWebSocketNotification(admin, notification);

                    log.info("예약 취소 알림 저장 및 전송 완료 - 관리자: {}, 예약: {}",
                            admin.getEmpNo(), reservation.getReservationNo());
                } catch (Exception e) {
                    log.error("관리자 {}에게 취소 알림 전송 실패", admin.getEmpNo(), e);
                }
            }
        } catch (Exception e) {
            log.error("예약 취소 알림 전송 실패", e);
        }
    }

    private void sendWebSocketNotification(Employee employee, Notification notification) {
        try {
            log.info("WebSocket 알림 전송 시작 - 수신자: {}, 알림: {}",
                    employee.getEmpId(), notification.getTitle());

            Map<String, Object> message = new HashMap<>();
            message.put("notificationNo", notification.getNotificationNo());
            message.put("type", notification.getType());
            message.put("title", notification.getTitle());
            message.put("content", notification.getContent());
            message.put("priority", notification.getPriority());
            message.put("createDate", notification.getCreateDate());
            message.put("refType", notification.getRefType());
            message.put("refNo", notification.getRefNo());
            message.put("recipientEmpNo", notification.getRecipientEmpNo());
            message.put("isRead", false);
            message.put("readDate", null);

            String personalDestination = "/user/" + employee.getEmpId() + "/queue/notifications";
            messagingTemplate.convertAndSend(personalDestination, message);
            log.info("empId 기반 개인 알림 전송: {}", personalDestination);

            String personalDestinationByEmpNo = "/user/" + employee.getEmpNo() + "/queue/notifications";
            messagingTemplate.convertAndSend(personalDestinationByEmpNo, message);
            log.info("empNo 기반 개인 알림 전송: {}", personalDestinationByEmpNo);

            messagingTemplate.convertAndSend("/topic/notifications", message);
            log.info("전체 알림 전송 완료");

        } catch (Exception e) {
            log.error("WebSocket 알림 전송 실패 - empNo: {}", employee.getEmpNo(), e);
        }
    }

    private void sendWebSocketNotificationWithType(Employee employee, Notification notification, String wsType) {
        try {
            log.info("WebSocket 알림 전송 시작 - 수신자: {}, 타입: {}", employee.getEmpId(), wsType);

            Map<String, Object> message = new HashMap<>();
            message.put("notificationNo", notification.getNotificationNo());
            message.put("type", wsType);
            message.put("title", notification.getTitle());
            message.put("content", notification.getContent());
            message.put("priority", notification.getPriority());
            message.put("createDate", notification.getCreateDate());
            message.put("refType", notification.getRefType());
            message.put("refNo", notification.getRefNo());
            message.put("recipientEmpNo", notification.getRecipientEmpNo());
            message.put("isRead", false);
            message.put("readDate", null);

            String personalDestination = "/user/" + employee.getEmpId() + "/queue/notifications";
            messagingTemplate.convertAndSend(personalDestination, message);
            log.info("empId 기반 개인 알림 전송 완료: {}", personalDestination);

            String personalDestinationByEmpNo = "/user/" + employee.getEmpNo() + "/queue/notifications";
            messagingTemplate.convertAndSend(personalDestinationByEmpNo, message);
            log.info("empNo 기반 개인 알림 전송 완료: {}", personalDestinationByEmpNo);

            messagingTemplate.convertAndSend("/topic/notifications", message);
            log.info("전체 알림 전송 완료");

        } catch (Exception e) {
            log.error("WebSocket 알림 전송 실패 - empNo: {}", employee.getEmpNo(), e);
        }
    }

    private void validateReservationRequest(ReservationRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("시작 시간은 현재 시간 이후여야 합니다.");
        }

        long minutes = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (minutes < 30) {
            throw new IllegalArgumentException("최소 30분 이상 예약해야 합니다.");
        }

        if (minutes > 480) {
            throw new IllegalArgumentException("최대 8시간까지 예약 가능합니다.");
        }
    }

    private String getStatusText(ReservationStatus status) {
        switch (status) {
            case APPROVED: return "승인";
            case CANCELED: return "거부";
            case PENDING: return "대기";
            default: return status.toString();
        }
    }

    private FacilityResponse convertToResponse(Facility facility) {
        return new FacilityResponse(
                facility.getFacilityNo(),
                facility.getFacilityName(),
                facility.getFacilityLocation(),
                facility.getCapacity(),
                facility.getDescription(),
                facility.getImagePath()
        );
    }

    private ReservationResponse convertToReservationResponse(FacilityReservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setReservationNo(reservation.getReservationNo());
        response.setFacilityNo(reservation.getFacilityNo());
        response.setFacilityName(reservation.getFacilityName());
        response.setReserverEmpNo(reservation.getReserverEmpNo());
        response.setReserverName(reservation.getReserverName());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());
        response.setPurpose(reservation.getPurpose());
        response.setStatus(reservation.getStatus());
        response.setCreateDate(reservation.getCreateDate());
        response.setUpdateDate(reservation.getUpdateDate());
        return response;
    }
}