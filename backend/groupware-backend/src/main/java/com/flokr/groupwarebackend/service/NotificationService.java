// NotificationService.java
package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.entity.Notification;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.repository.NotificationRepository;
import com.flokr.groupwarebackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 읽지 않은 알림 목록 조회
     */
    public List<Notification> getUnreadNotifications(Long empNo) {
        return notificationRepository.findByRecipientEmpNoAndReadDateIsNullOrderByCreateDateDesc(empNo);
    }

    /**
     * 모든 알림 목록 조회 (페이징)
     */
    public Page<Notification> getAllNotifications(Long empNo, Pageable pageable) {
        return notificationRepository.findByRecipientEmpNoOrderByCreateDateDesc(empNo, pageable);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    public long getUnreadNotificationCount(Long empNo) {
        return notificationRepository.countByRecipientEmpNoAndReadDateIsNull(empNo);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public boolean markAsRead(Long notificationNo, Long empNo) {
        try {
            Optional<Notification> notificationOpt = notificationRepository
                    .findByNotificationNoAndRecipientEmpNo(notificationNo, empNo);

            if (notificationOpt.isPresent()) {
                Notification notification = notificationOpt.get();
                if (notification.getReadDate() == null) {
                    notification.markAsRead();
                    notificationRepository.save(notification);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패: notificationNo={}, empNo={}", notificationNo, empNo, e);
            return false;
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public int markAllAsRead(Long empNo) {
        try {
            List<Notification> unreadNotifications = notificationRepository
                    .findByRecipientEmpNoAndReadDateIsNull(empNo);

            LocalDateTime now = LocalDateTime.now();
            for (Notification notification : unreadNotifications) {
                notification.setReadDate(now);
            }

            notificationRepository.saveAll(unreadNotifications);
            return unreadNotifications.size();
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패: empNo={}", empNo, e);
            return 0;
        }
    }

    /**
     * 알림 생성 및 저장 (WebSocket 전송 포함)
     */
    @Transactional
    public Notification createNotification(Long recipientEmpNo, String type, String title,
                                           String content, String refType, String refNo) {
        try {
            Notification notification = new Notification(
                    recipientEmpNo, type, title, content, refType, refNo, "NORMAL"
            );

            Notification savedNotification = notificationRepository.save(notification);
            log.info("알림 생성 성공: notificationNo={}, recipientEmpNo={}, type={}",
                    savedNotification.getNotificationNo(), recipientEmpNo, type);

            // WebSocket으로 실시간 알림 전송
            sendWebSocketNotification(savedNotification);

            return savedNotification;
        } catch (Exception e) {
            log.error("알림 생성 실패: recipientEmpNo={}, type={}, title={}",
                    recipientEmpNo, type, title, e);
            return null;
        }
    }

    /**
     * 시설 예약 승인 알림
     */
    @Transactional
    public void sendFacilityApprovedNotification(Long empNo, String facilityName) {
        createNotification(
                empNo,
                "FACILITY_APPROVED",
                "시설 예약 승인",
                facilityName + " 예약이 승인되었습니다.",
                "FACILITY_RESERVATION",
                null
        );
        log.info("시설 예약 승인 알림 전송: empNo={}, facilityName={}", empNo, facilityName);
    }

    /**
     * 시설 예약 거절 알림
     */
    @Transactional
    public void sendFacilityRejectedNotification(Long empNo, String facilityName) {
        createNotification(
                empNo,
                "FACILITY_REJECTED",
                "시설 예약 거절",
                facilityName + " 예약이 거절되었습니다.",
                "FACILITY_RESERVATION",
                null
        );
        log.info("시설 예약 거절 알림 전송: empNo={}, facilityName={}", empNo, facilityName);
    }

    /**
     * 오래된 알림 삭제
     */
    @Transactional
    public int deleteOldNotifications(int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            List<Notification> oldNotifications = notificationRepository
                    .findByCreateDateBefore(cutoffDate);

            notificationRepository.deleteAll(oldNotifications);
            return oldNotifications.size();
        } catch (Exception e) {
            log.error("오래된 알림 삭제 실패: days={}", days, e);
            return 0;
        }
    }

    /**
     * 관리자 알림 발송 내역 조회 (페이징, 필터링)
     */
    public Page<Notification> getAdminNotificationHistory(Pageable pageable, String type, String keyword) {
        try {
            List<Notification> allNotifications = notificationRepository.findAll();

            List<Notification> filteredNotifications = allNotifications.stream()
                    .filter(n -> type == null || type.isEmpty() || type.equals(n.getType()))
                    .filter(n -> keyword == null || keyword.isEmpty() ||
                            n.getTitle().contains(keyword) || n.getContent().contains(keyword))
                    .sorted((n1, n2) -> n2.getCreateDate().compareTo(n1.getCreateDate()))
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredNotifications.size());

            List<Notification> pageContent = filteredNotifications.subList(start, end);

            return new PageImpl<>(pageContent, pageable, filteredNotifications.size());
        } catch (Exception e) {
            log.error("관리자 알림 발송 내역 조회 실패", e);
            return Page.empty(pageable);
        }
    }

    /**
     * WebSocket으로 실시간 알림 전송
     */
    private void sendWebSocketNotification(Notification notification) {
        try {
            String empId = getEmpIdByEmpNo(notification.getRecipientEmpNo());
            if (empId != null) {
                // 개인 알림 전송
                String personalDestination = "/user/" + empId + "/queue/notifications";
                messagingTemplate.convertAndSend(personalDestination, notification);
                log.info("개인 WebSocket 알림 전송 완료: {} -> {}", personalDestination, notification.getTitle());

                // 전체 알림도 전송
                messagingTemplate.convertAndSend("/topic/notifications", notification);
                log.info("전체 WebSocket 알림 전송 완료: /topic/notifications -> {}", notification.getTitle());
            }
        } catch (Exception e) {
            log.error("WebSocket 알림 전송 실패: notificationNo={}", notification.getNotificationNo(), e);
        }
    }

    /**
     * empNo로 empId 조회 (실제 구현)
     */
    private String getEmpIdByEmpNo(Long empNo) {
        try {
            Optional<Employee> employee = employeeRepository.findById(empNo);
            if (employee.isPresent()) {
                String empId = employee.get().getEmpId();
                log.debug("empNo {} -> empId {}", empNo, empId);
                return empId;
            }
            log.warn("empNo {}에 해당하는 직원을 찾을 수 없습니다", empNo);
            return null;
        } catch (Exception e) {
            log.error("empId 조회 실패: empNo={}", empNo, e);
            return null;
        }
    }
}