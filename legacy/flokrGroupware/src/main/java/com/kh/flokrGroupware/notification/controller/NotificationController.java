package com.kh.flokrGroupware.notification.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.common.template.Pagination;
import com.kh.flokrGroupware.employee.model.service.EmployeeService;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.notification.model.service.NotificationService;
import com.kh.flokrGroupware.notification.model.service.NotificationSenderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationSenderService notificationSenderService;
    
    @Autowired
    private EmployeeService employeeService;
    
    // 모든 알림 조회 페이지
    @GetMapping("/notificationAll")
    public String allNotifications(
            HttpSession session, 
            Model model,
            @RequestParam(value="page", defaultValue="1") int currentPage) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null) {
            return "redirect:/";
        }
        
        // 페이지당 표시할 알림 수
        int limit = 10;
        
        // 특정 사용자의 총 알림 수 조회 (중요: 로그인한 사용자의 알림만 카운트해야 함)
        int listCount = notificationService.getUserNotificationsCount(loginUser.getEmpNo());
        
        // 페이지 정보 설정 (Pagination 템플릿 사용)
        PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 5, limit);
        
        // 실제 데이터가 있는 페이지 범위를 넘어가면 마지막 페이지로 리다이렉트
        if(listCount > 0 && currentPage > pi.getMaxPage()) {
            return "redirect:/notificationAll?page=" + pi.getMaxPage();
        }
        
        // 알림 목록 조회
        List<Map<String, Object>> notifications = notificationService.getAllNotificationsPaging(loginUser.getEmpNo(), currentPage, limit);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("maxPage", pi.getMaxPage());
        model.addAttribute("startPage", pi.getStartPage());
        model.addAttribute("endPage", pi.getEndPage());
        
        return "notification/notificationAll";
    }
    
    // 알림 읽음 처리 (AJAX)
    @PostMapping("/notificationRead/{notificationNo}")
    @ResponseBody
    public Map<String, Object> readNotification(@PathVariable int notificationNo, HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null) {
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }
        
        try {
            notificationService.markAsRead(notificationNo, loginUser.getEmpNo());
            return Map.of("success", true);
        } catch(Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    // 알림 개수 조회 (AJAX)
    @GetMapping("/notificationCount")
    @ResponseBody
    public int getUnreadNotificationCount(HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null) {
            return 0;
        }
        
        List<Map<String, Object>> unreadNotifications = notificationService.getUnreadNotifications(loginUser.getEmpNo());
        return unreadNotifications.size();
    }
    
    // 관리자용 알림 관리 페이지
    @GetMapping("/notificationAdmin")
    public String adminNotifications(
            HttpSession session, 
            Model model,
            @RequestParam(value="page", defaultValue="1") int currentPage,
            @RequestParam(value="type", required=false) String type,
            @RequestParam(value="keyword", required=false) String keyword,
            @RequestParam(value="tab", required=false) String tab) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자 권한이 필요합니다.");
            return "redirect:/";
        }
        
        // 페이지당 표시할 알림 수
        int limit = 10;
        
        // 필터 조건에 맞는 총 알림 수 조회
        int listCount = notificationService.getTotalNotificationsCount(type, keyword);
        
        // 페이지 정보 설정 (Pagination 템플릿 사용)
        PageInfo pi = Pagination.getPageInfo(listCount, currentPage, 5, limit);
        
        // 실제 데이터가 있는 페이지 범위를 넘어가면 마지막 페이지로 리다이렉트
        if(listCount > 0 && currentPage > pi.getMaxPage()) {
            String redirect = "redirect:/notificationAdmin?page=" + pi.getMaxPage();
            if(type != null) redirect += "&type=" + type;
            if(keyword != null) redirect += "&keyword=" + keyword;
            if(tab != null) redirect += "&tab=" + tab;
            return redirect;
        }
        
        // 알림 목록 조회
        List<Map<String, Object>> notifications = notificationService.getNotificationsForAdmin(currentPage, limit, type, keyword);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("maxPage", pi.getMaxPage());
        model.addAttribute("startPage", pi.getStartPage());
        model.addAttribute("endPage", pi.getEndPage());
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("tab", tab);
        
        // 부서 목록 조회 (알림 발송용)
        model.addAttribute("departments", employeeService.selectDepartmentList());
        
        // 현재 페이지를 문자열로 전달 (헤더 메뉴 active 표시용)
        model.addAttribute("currentMenu", "notification");
        
        return "notification/notificationAdmin";
    }
    
    // 관리자용 알림 발송 기능
    @PostMapping("/notificationAdminSend")
    @ResponseBody
    public Map<String, Object> sendNotificationAdmin(
            HttpSession session,
            @RequestParam("targetType") String targetType,
            @RequestParam(value="targetId", required=false) String targetId,
            @RequestParam("notificationType") String notificationType,
            @RequestParam("title") String title,
            @RequestParam(value="content", required=false) String content,
            @RequestParam(value="refType", required=false) String refType,
            @RequestParam(value="refNo", required=false) String refNo) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            return Map.of("success", false, "message", "관리자 권한이 필요합니다.");
        }
        
        // 로그 형식
        logger.info("알림 발송 요청: targetType=" + targetType + ", targetId=" + targetId + 
                ", type=" + notificationType + ", title=" + title);
        
        try {
            if("ALL".equals(targetType)) {
                // 전체 직원에게 알림 발송
                notificationSenderService.sendNotificationToAll(notificationType, title, content, refType, refNo);
                return Map.of("success", true, "message", "전체 직원에게 알림이 발송되었습니다.");
            } else if("DEPARTMENT".equals(targetType) && targetId != null) {
                // 특정 부서에게 알림 발송
                int deptNo = Integer.parseInt(targetId);
                notificationSenderService.sendNotificationToDepartment(deptNo, notificationType, title, content, refType, refNo);
                return Map.of("success", true, "message", "선택한 부서 직원에게 알림이 발송되었습니다.");
            } else if("EMPLOYEE".equals(targetType) && targetId != null) {
                // 특정 직원에게 알림 발송
                int empNo = Integer.parseInt(targetId);
                
                // 직원 기본 조회
                Employee employee = employeeService.selectEmployee(empNo);
                String empId = employee != null ? employee.getEmpId() : null;
                
                notificationSenderService.sendNotificationToUser(empNo, empId, notificationType, title, content, refType, refNo);
                return Map.of("success", true, "message", "선택한 직원에게 알림이 발송되었습니다.");
            } else {
                logger.warn("올바르지 않은 알림 대상: targetType=" + targetType + ", targetId=" + targetId);
                return Map.of("success", false, "message", "올바르지 않은 대상 유형입니다.");
            }
        } catch(NumberFormatException e) {
            logger.error("알림 발송 중 숫자 변환 오류: " + e.getMessage());
            return Map.of("success", false, "message", "대상 ID가 올바르지 않습니다.");
        } catch(Exception e) {
            logger.error("알림 발송 중 오류 발생", e);
            return Map.of("success", false, "message", "알림 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Object getValueOrDefault(Map<String, Object> map, Object defaultValue, String... possibleKeys) {
        if (map == null) return defaultValue;
        
        // 직접 키 매칭
        for (String key : possibleKeys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        
        // 대소문자 구분 없는 키 매칭
        for (String key : possibleKeys) {
            for (String mapKey : map.keySet()) {
                if (mapKey.equalsIgnoreCase(key)) {
                    return map.get(mapKey);
                }
            }
        }
        
        return defaultValue;
    }
    
    // 알림 통계 조회
    @GetMapping("/notificationAdminStats")
    @ResponseBody
    public Map<String, Object> getNotificationStatistics(HttpSession session) {
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            return Map.of("success", false, "message", "관리자 권한이 필요합니다.");
        }
        
        // 여기에 알림 통계 조회 로직을 추가 (필요시)
        
        return Map.of("success", true, "data", Map.of(
            "totalSent", 150,
            "totalRead", 120,
            "readRate", 80,
            "todaySent", 15,
            "byType", Map.of("NOTICE", 50, "APPROVAL", 40, "CHAT", 30, "TASK", 30)
        ));
    }
    
    // 오래된 알림 정리 (수동)
    @PostMapping("/notificationAdminCleanup")
    @ResponseBody
    public Map<String, Object> cleanupOldNotifications(
            HttpSession session,
            @RequestParam("days") int days) {
        
        Employee loginUser = (Employee) session.getAttribute("loginUser");
        
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            return Map.of("success", false, "message", "관리자 권한이 필요합니다.");
        }
        
        try {
            notificationService.deleteOldNotifications(days);
            return Map.of("success", true, "message", days + "일이 지난 알림을 정리했습니다.");
        } catch(Exception e) {
            return Map.of("success", false, "message", "알림 정리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}