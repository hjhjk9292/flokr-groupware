// NotificationController.java
package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.NotificationCreateRequest;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.entity.Notification;
import com.flokr.groupwarebackend.service.NotificationService;
import com.flokr.groupwarebackend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    private final NotificationService notificationService;
    private final EmployeeService employeeService;

    // ========== 사용자 알림 API ==========

    /**
     * 읽지 않은 알림 목록 조회
     */
    @GetMapping("/notifications/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.<List<Notification>>builder()
                    .success(false)
                    .message("인증이 필요합니다.")
                    .build());
        }

        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(currentUser.getEmpNo());

            return ResponseEntity.ok(ApiResponse.<List<Notification>>builder()
                    .success(true)
                    .message("읽지 않은 알림 목록 조회 성공")
                    .data(notifications)
                    .build());
        } catch (Exception e) {
            log.error("읽지 않은 알림 조회 오류", e);
            return ResponseEntity.status(500).body(ApiResponse.<List<Notification>>builder()
                    .success(false)
                    .message("알림 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .data(0L)
                    .build());
        }

        try {
            long count = notificationService.getUnreadNotificationCount(currentUser.getEmpNo());
            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .message("읽지 않은 알림 개수 조회 성공")
                    .data(count)
                    .build());
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 오류", e);
            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .success(true)
                    .data(0L)
                    .build());
        }
    }

    /**
     * 모든 알림 목록 조회 (페이징)
     */
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Page<Notification>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.<Page<Notification>>builder()
                    .success(false)
                    .message("인증이 필요합니다.")
                    .build());
        }

        try {
            Page<Notification> notifications = notificationService.getAllNotifications(
                    currentUser.getEmpNo(), PageRequest.of(page, size));

            return ResponseEntity.ok(ApiResponse.<Page<Notification>>builder()
                    .success(true)
                    .message("모든 알림 목록 조회 성공")
                    .data(notifications)
                    .build());
        } catch (Exception e) {
            log.error("모든 알림 조회 오류", e);
            return ResponseEntity.status(500).body(ApiResponse.<Page<Notification>>builder()
                    .success(false)
                    .message("알림 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/notifications/{notificationNo}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @PathVariable Long notificationNo,
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("인증이 필요합니다.")
                    .build());
        }

        try {
            boolean success = notificationService.markAsRead(notificationNo, currentUser.getEmpNo());
            if (success) {
                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .success(true)
                        .message("알림 읽음 처리 완료")
                        .build());
            } else {
                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .success(false)
                        .message("알림을 찾을 수 없습니다.")
                        .build());
            }
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패", e);
            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("알림 읽음 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PutMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("인증이 필요합니다.")
                    .build());
        }

        try {
            int count = notificationService.markAllAsRead(currentUser.getEmpNo());
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message(count + "개의 알림을 읽음 처리했습니다.")
                    .build());
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패", e);
            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("모든 알림 읽음 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    // ========== 관리자 알림 API ==========

    /**
     * 관리자 알림 생성 및 발송
     */
    @PostMapping("/admin/notifications")
    public ResponseEntity<ApiResponse<String>> createNotification(
            @RequestBody NotificationCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null || !isAdmin(currentUser)) {
            return ResponseEntity.status(401).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("관리자 권한이 필요합니다.")
                    .build());
        }

        try {
            log.info("관리자 알림 생성 요청: {}", request);

            if ("ALL".equals(request.getTargetType())) {
                // 전체 발송
                List<Employee> allEmployees = employeeService.getAllActiveEmployees();
                for (Employee employee : allEmployees) {
                    notificationService.createNotification(
                            employee.getEmpNo(),
                            request.getType(),
                            request.getTitle(),
                            request.getContent(),
                            request.getRefType(),
                            request.getRefNo()
                    );
                }
                log.info("전체 알림 발송 완료: {} 명", allEmployees.size());
                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .success(true)
                        .message(allEmployees.size() + "명에게 알림을 발송했습니다.")
                        .build());

            } else if ("EMPLOYEE".equals(request.getTargetType())) {
                // 개별 발송
                Long empNo = Long.parseLong(request.getTargetId());
                Optional<Employee> employeeOpt = employeeService.getEmployeeByEmpNo(empNo);

                if (employeeOpt.isPresent()) {
                    notificationService.createNotification(
                            empNo,
                            request.getType(),
                            request.getTitle(),
                            request.getContent(),
                            request.getRefType(),
                            request.getRefNo()
                    );
                    log.info("개별 알림 발송 완료: empNo={}", empNo);
                    return ResponseEntity.ok(ApiResponse.<String>builder()
                            .success(true)
                            .message("알림을 발송했습니다.")
                            .build());
                } else {
                    return ResponseEntity.ok(ApiResponse.<String>builder()
                            .success(false)
                            .message("직원을 찾을 수 없습니다.")
                            .build());
                }

            } else if ("DEPARTMENT".equals(request.getTargetType())) {
                // 부서별 발송
                Long deptNo = Long.parseLong(request.getTargetId());
                List<Employee> deptEmployees = employeeService.getEmployeesByDepartment(deptNo);

                for (Employee employee : deptEmployees) {
                    notificationService.createNotification(
                            employee.getEmpNo(),
                            request.getType(),
                            request.getTitle(),
                            request.getContent(),
                            request.getRefType(),
                            request.getRefNo()
                    );
                }
                log.info("부서별 알림 발송 완료: deptNo={}, 발송 수={}", deptNo, deptEmployees.size());
                return ResponseEntity.ok(ApiResponse.<String>builder()
                        .success(true)
                        .message(deptEmployees.size() + "명에게 알림을 발송했습니다.")
                        .build());
            }

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(false)
                    .message("올바르지 않은 발송 타입입니다.")
                    .build());

        } catch (Exception e) {
            log.error("알림 생성 실패", e);
            return ResponseEntity.status(500).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("알림 생성 중 오류가 발생했습니다: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 관리자 알림 발송 내역 조회
     */
    @GetMapping("/admin/notifications/history")
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotificationHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null || !isAdmin(currentUser)) {
            return ResponseEntity.status(401).body(ApiResponse.<Page<Notification>>builder()
                    .success(false)
                    .message("관리자 권한이 필요합니다.")
                    .build());
        }

        try {
            Page<Notification> notifications = notificationService.getAdminNotificationHistory(
                    PageRequest.of(page, size), type, keyword);

            return ResponseEntity.ok(ApiResponse.<Page<Notification>>builder()
                    .success(true)
                    .message("알림 발송 내역 조회 성공")
                    .data(notifications)
                    .build());
        } catch (Exception e) {
            log.error("알림 발송 내역 조회 오류", e);
            return ResponseEntity.status(500).body(ApiResponse.<Page<Notification>>builder()
                    .success(false)
                    .message("알림 발송 내역 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 직원 검색
     */
    @GetMapping("/admin/notifications/employees/search")
    public ResponseEntity<ApiResponse<List<Employee>>> searchEmployees(
            @RequestParam String keyword,
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null || !isAdmin(currentUser)) {
            return ResponseEntity.status(401).body(ApiResponse.<List<Employee>>builder()
                    .success(false)
                    .message("관리자 권한이 필요합니다.")
                    .build());
        }

        try {
            List<Employee> employees = employeeService.searchEmployees(keyword);
            return ResponseEntity.ok(ApiResponse.<List<Employee>>builder()
                    .success(true)
                    .message("직원 검색 성공")
                    .data(employees)
                    .build());
        } catch (Exception e) {
            log.error("직원 검색 오류", e);
            return ResponseEntity.status(500).body(ApiResponse.<List<Employee>>builder()
                    .success(false)
                    .message("직원 검색 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 부서별 직원 목록 조회
     */
    @GetMapping("/admin/notifications/departments/{deptNo}/employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getDepartmentEmployees(
            @PathVariable Long deptNo,
            @AuthenticationPrincipal UserDetails principal) {

        Employee currentUser = getCurrentUser(principal);
        if (currentUser == null || !isAdmin(currentUser)) {
            return ResponseEntity.status(401).body(ApiResponse.<List<Employee>>builder()
                    .success(false)
                    .message("관리자 권한이 필요합니다.")
                    .build());
        }

        try {
            List<Employee> employees = employeeService.getEmployeesByDepartment(deptNo);
            return ResponseEntity.ok(ApiResponse.<List<Employee>>builder()
                    .success(true)
                    .message("부서별 직원 목록 조회 성공")
                    .data(employees)
                    .build());
        } catch (Exception e) {
            log.error("부서별 직원 목록 조회 오류", e);
            return ResponseEntity.status(500).body(ApiResponse.<List<Employee>>builder()
                    .success(false)
                    .message("부서별 직원 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 현재 사용자 정보 추출
     */
    private Employee getCurrentUser(UserDetails principal) {
        if (principal == null) {
            return null;
        }
        String empId = principal.getUsername();
        Optional<Employee> currentUserOpt = employeeService.getEmployeeByEmpId(empId);
        return currentUserOpt.orElse(null);
    }

    /**
     * 관리자 권한 확인
     */
    private boolean isAdmin(Employee employee) {
        return employee != null && employee.getIsAdmin() != null && "Y".equals(employee.getIsAdmin());
    }
}