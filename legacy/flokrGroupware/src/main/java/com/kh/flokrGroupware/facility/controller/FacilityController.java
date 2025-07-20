package com.kh.flokrGroupware.facility.controller;

import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.kh.flokrGroupware.employee.model.service.EmployeeService;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.facility.model.service.FacilityService;
import com.kh.flokrGroupware.facility.model.vo.Facility;
import com.kh.flokrGroupware.notification.model.service.NotificationSenderService;

/**
 * 시설 및 예약 관리 컨트롤러
 */
@Controller
public class FacilityController {
    
    private static final Logger logger = LoggerFactory.getLogger(FacilityController.class);
    
    @Autowired
    private FacilityService facilityService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private NotificationSenderService notificationSenderService;
    
    //--------------------------------
    // 관리자 시설 관리 페이지
    //--------------------------------
    
    /**
     * 관리자 시설 관리 페이지
     */
    @GetMapping("adminFacility")
    public String adminFacility(Model model, HttpSession session) {
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 시설 관리 페이지 접근 시도");
            return "redirect:/";
        }
        
        try {
            // 시설 목록 조회
            List<Facility> facilities = facilityService.selectFacilityList();
            model.addAttribute("facilities", facilities);
            
            // 예약 목록 조회 (최근 30일 데이터)
            List<Map<String, Object>> reservations = facilityService.selectRecentReservations(30);
            model.addAttribute("reservations", reservations);
            
            // 현재 메뉴 설정
            model.addAttribute("currentMenu", "facility");
            
            logger.info("시설 관리 페이지 접근 - 관리자: " + loginUser.getEmpId());
            return "facility/adminFacility";
            
        } catch (Exception e) {
            logger.error("시설 관리 페이지 로드 중 오류 발생: " + e.getMessage(), e);
            session.setAttribute("alertMsg", "시설 관리 페이지 로드 중 오류가 발생했습니다.");
            return "redirect:/adminMain";
        }
    }
    
    /**
     * 시설 목록 조회 (AJAX)
     */
    @GetMapping("getFacilities")
    @ResponseBody
    public Map<String, Object> getFacilities(
            @RequestParam(value="keyword", required=false) String keyword,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 시설 목록 조회 시도");
            return response;
        }
        
        try {
            // 검색 조건이 있는 경우
            List<Facility> facilities;
            if(keyword != null && !keyword.trim().isEmpty()) {
                facilities = facilityService.searchFacilities(keyword);
            } else {
                facilities = facilityService.selectFacilityList();
            }
            
            response.put("success", true);
            response.put("facilities", facilities);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "시설 목록 조회 중 오류가 발생했습니다.");
            logger.error("시설 목록 조회 중 오류 발생: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 시설 상세 정보 조회 (AJAX)
     */
    @GetMapping("getFacilityDetail")
    @ResponseBody
    public Map<String, Object> getFacilityDetail(
            @RequestParam("facilityNo") int facilityNo,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 시설 상세 정보 조회 시도 - 시설번호: " + facilityNo);
            return response;
        }
        
        try {
            // 시설 정보 조회
            Facility facility = facilityService.selectFacility(facilityNo);
            
            if(facility != null) {
                response.put("success", true);
                response.put("facility", facility);
            } else {
                response.put("success", false);
                response.put("message", "해당 시설 정보를 찾을 수 없습니다.");
                logger.warn("존재하지 않는 시설 상세 정보 조회 시도 - 시설번호: " + facilityNo);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "시설 상세 정보 조회 중 오류가 발생했습니다.");
            logger.error("시설 상세 정보 조회 중 오류 발생 - 시설번호: " + facilityNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 시설 추가
     */
    @PostMapping("addFacility")
    @ResponseBody
    public Map<String, Object> addFacility(
            Facility facility, 
            @RequestParam(value="file", required=false) MultipartFile file,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 시설 추가 시도");
            return response;
        }
        
        try {
            logger.info("시설 추가 시도 - 관리자: " + loginUser.getEmpId() + ", 시설명: " + facility.getFacilityName());
            
            int result = facilityService.insertFacility(facility);
            
            if(result > 0) {
                response.put("success", true);
                response.put("message", "시설이 추가되었습니다.");
                response.put("facilityNo", facility.getFacilityNo());
                logger.info("시설 추가 성공 - 시설번호: " + facility.getFacilityNo() + ", 시설명: " + facility.getFacilityName());
            } else {
                response.put("success", false);
                response.put("message", "시설 추가에 실패했습니다.");
                logger.error("시설 추가 실패 - 시설명: " + facility.getFacilityName());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "시설 추가 중 오류가 발생했습니다.");
            logger.error("시설 추가 중 오류 발생 - 시설명: " + facility.getFacilityName() + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 시설 정보 수정
     */
    @PostMapping("updateFacility")
    @ResponseBody
    public Map<String, Object> updateFacility(
            Facility facility, 
            @RequestParam(value="file", required=false) MultipartFile file,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 시설 정보 수정 시도 - 시설번호: " + facility.getFacilityNo());
            return response;
        }
        
        try {
            
            int result = facilityService.updateFacility(facility);
            
            if(result > 0) {
                response.put("success", true);
                response.put("message", "시설 정보가 수정되었습니다.");
                logger.info("시설 정보 수정 성공 - 시설번호: " + facility.getFacilityNo());
            } else {
                response.put("success", false);
                response.put("message", "시설 정보 수정에 실패했습니다.");
                logger.error("시설 정보 수정 실패 - 시설번호: " + facility.getFacilityNo());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "시설 정보 수정 중 오류가 발생했습니다.");
            logger.error("시설 정보 수정 중 오류 발생 - 시설번호: " + facility.getFacilityNo() + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 시설 삭제
     */
    @PostMapping("deleteFacility")
    @ResponseBody
    public Map<String, Object> deleteFacility(
            @RequestParam("facilityNo") int facilityNo,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 시설 삭제 시도 - 시설번호: " + facilityNo);
            return response;
        }
        
        try {
            // 해당 시설의 예약 유무 확인
            int reservationCount = facilityService.getReservationCountByFacility(facilityNo);
            
            if(reservationCount > 0) {
                response.put("success", false);
                response.put("message", "해당 시설에 예약 내역이 있어 삭제할 수 없습니다.");
                logger.warn("예약 내역이 있는 시설 삭제 시도 - 시설번호: " + facilityNo + ", 예약 수: " + reservationCount);
                return response;
            }
            
            // 이미지 파일 삭제
            Facility facility = facilityService.selectFacility(facilityNo);
            if(facility != null && facility.getImagePath() != null) {
                String realPath = session.getServletContext().getRealPath(facility.getImagePath());
                new File(realPath).delete();
            }
            
            int result = facilityService.deleteFacility(facilityNo);
            
            if(result > 0) {
                response.put("success", true);
                response.put("message", "시설이 삭제되었습니다.");
                logger.info("시설 삭제 성공 - 시설번호: " + facilityNo);
            } else {
                response.put("success", false);
                response.put("message", "시설 삭제에 실패했습니다.");
                logger.error("시설 삭제 실패 - 시설번호: " + facilityNo);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "시설 삭제 중 오류가 발생했습니다.");
            logger.error("시설 삭제 중 오류 발생 - 시설번호: " + facilityNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 예약 목록 조회 (AJAX)
     */
    @GetMapping("getReservations")
    @ResponseBody
    public Map<String, Object> getReservations(
            @RequestParam(value="facilityNo", required=false) Integer facilityNo,
            @RequestParam(value="status", required=false) String status,
            @RequestParam(value="date", required=false) String date,
            @RequestParam(value="keyword", required=false) String keyword,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 예약 목록 조회 시도");
            return response;
        }
        
        try {
            Map<String, Object> params = new HashMap<>();
            
            if(facilityNo != null) {
                params.put("facilityNo", facilityNo);
            }
            
            if(status != null && !status.isEmpty()) {
                params.put("status", status);
            }
            
            if(date != null && !date.isEmpty()) {
                params.put("date", date);
            }
            
            if(keyword != null && !keyword.trim().isEmpty()) {
                params.put("keyword", keyword.trim());
            }
            
            List<Map<String, Object>> reservations = facilityService.selectReservationsByCondition(params);
            
            response.put("success", true);
            response.put("reservations", reservations);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "예약 목록 조회 중 오류가 발생했습니다.");
            logger.error("예약 목록 조회 중 오류 발생: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 예약 상세 정보 조회 (AJAX)
     */
    @GetMapping("getReservationDetail")
    @ResponseBody
    public Map<String, Object> getReservationDetail(
            @RequestParam("reservationNo") int reservationNo,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 예약 상세 정보 조회 시도 - 예약번호: " + reservationNo);
            return response;
        }
        
        try {
            // 예약 정보 조회
            Map<String, Object> reservation = facilityService.selectReservationDetail(reservationNo);
            
            if(reservation != null) {
                // 관리자가 아닌 경우, 본인의 예약인지 확인
                if(!"Y".equals(loginUser.getIsAdmin())) {
                    int reserverEmpNo = ((Number)reservation.get("RESERVER_EMP_NO")).intValue();
                    if(reserverEmpNo != loginUser.getEmpNo()) {
                        response.put("success", false);
                        response.put("message", "본인의 예약만 확인할 수 있습니다.");
                        logger.warn("타인의 예약 상세 정보 조회 시도 - 사용자: " + loginUser.getEmpId() + ", 예약번호: " + reservationNo);
                        return response;
                    }
                }
                
                response.put("success", true);
                response.put("reservation", reservation);
            } else {
                response.put("success", false);
                response.put("message", "해당 예약 정보를 찾을 수 없습니다.");
                logger.warn("존재하지 않는 예약 상세 정보 조회 시도 - 예약번호: " + reservationNo);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "예약 상세 정보 조회 중 오류가 발생했습니다.");
            logger.error("예약 상세 정보 조회 중 오류 발생 - 예약번호: " + reservationNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 예약 상태 변경 (승인/취소)
     */
    @PostMapping("updateReservationStatus")
    @ResponseBody
    public Map<String, Object> updateReservationStatus(
            @RequestParam("reservationNo") int reservationNo,
            @RequestParam("status") String status,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 예약 상태 변경 시도 - 예약번호: " + reservationNo);
            return response;
        }
        
        logger.info("예약 상태 변경 요청 - 예약번호: " + reservationNo + ", 상태 변경: " + status + 
                ", 요청자: " + loginUser.getEmpId() + ", 관리자 여부: " + loginUser.getIsAdmin());
        
        try {
            // 예약 정보 조회 (사용자 정보 및 시설 정보 포함)
            Map<String, Object> reservation = facilityService.selectReservationDetail(reservationNo);
            
            if (reservation == null) {
                response.put("success", false);
                response.put("message", "해당 예약 정보를 찾을 수 없습니다.");
                logger.warn("존재하지 않는 예약 상태 변경 시도 - 예약번호: " + reservationNo);
                return response;
            }
            
            // 관리자가 아닌 경우, 본인의 예약인지 & 취소만 가능한지 확인
            if(!"Y".equals(loginUser.getIsAdmin())) {
                // 본인 예약 확인
                int reserverEmpNo = ((Number)reservation.get("RESERVER_EMP_NO")).intValue();
                if(reserverEmpNo != loginUser.getEmpNo()) {
                    response.put("success", false);
                    response.put("message", "본인의 예약만 취소할 수 있습니다.");
                    logger.warn("타인의 예약 상태 변경 시도 - 사용자: " + loginUser.getEmpId() + ", 예약번호: " + reservationNo);
                    return response;
                }
                
                // 사용자는 취소만 가능
                if(!status.equals("CANCELED")) {
                    response.put("success", false);
                    response.put("message", "예약 취소만 가능합니다.");
                    logger.warn("사용자의 예약 승인/완료 시도 - 사용자: " + loginUser.getEmpId() + ", 예약번호: " + reservationNo);
                    return response;
                }
            }
            
            logger.info("예약 상태 변경 시도 - 예약번호: " + reservationNo + ", 상태: " + status);
            
            Map<String, Object> params = new HashMap<>();
            params.put("reservationNo", reservationNo);
            params.put("status", status);
            
            int result = facilityService.updateReservationStatus(params);
            
            if(result > 0) {
                // 알림 발송 처리
                try {
                    // 시설명 가져오기
                    String facilityName = (String) reservation.get("FACILITY_NAME");
                    
                    // 예약자 정보 가져오기
                    int reserverEmpNo = ((Number)reservation.get("RESERVER_EMP_NO")).intValue();
                    
                    // 예약자 ID 가져오기
                    String reserverId = null;
                    if(reservation.containsKey("RESERVER_ID")) {
                        reserverId = (String)reservation.get("RESERVER_ID");
                    } else {
                        // 예약자 정보가 없으면 사원 정보에서 조회
                        Employee reserver = employeeService.selectEmployee(reserverEmpNo);
                        if(reserver != null) {
                            reserverId = reserver.getEmpId();
                        }
                    }
                    
                    // 알림 제목과 내용 설정
                    String notificationTitle;
                    String notificationContent;
                    
                    // 관리자인 경우와 사용자인 경우를 분리하여 처리
                    if("Y".equals(loginUser.getIsAdmin())) {
                        // 관리자가 상태 변경하는 경우, 예약자에게 알림 발송
                        if(loginUser.getEmpNo() != reserverEmpNo && reserverId != null) {
                            if(status.equals("APPROVED")) {
                                notificationTitle = "시설 예약 승인 완료";
                                notificationContent = facilityName + " 예약이 승인되었습니다.";
                            } else if(status.equals("CANCELED")) {
                                notificationTitle = "시설 예약 승인 거절";
                                notificationContent = facilityName + " 예약 요청이 거절되었습니다.";
                            } else {
                                notificationTitle = "시설 예약 상태 변경";
                                notificationContent = facilityName + " 예약 상태가 변경되었습니다.";
                            }
                            
                            logger.info("알림 발송: 예약자(" + reserverId + ")에게 " + notificationTitle + " 알림 전송");
                            
                            notificationSenderService.sendNotificationToUser(
                                reserverEmpNo,
                                reserverId,
                                "FACILITY",
                                notificationTitle,
                                notificationContent,
                                "facility",
                                String.valueOf(reservationNo)
                            );
                        }
                    } else {
                        // 일반 사용자가 취소하는 경우, 관리자에게 알림 발송
                        if(status.equals("CANCELED")) {
                            notificationTitle = "시설 예약 취소";
                            notificationContent = loginUser.getEmpName() + "님이 " + facilityName + " 예약을 취소했습니다.";
                            
                            // 관리자에게 알림 발송 (관리자 ID가 1이라고 가정)
                            Employee admin = employeeService.selectEmployee(1);
                            if(admin != null) {
                                logger.info("알림 발송: 관리자에게 예약 취소 알림 전송");
                                
                                notificationSenderService.sendNotificationToUser(
                                    admin.getEmpNo(),
                                    admin.getEmpId(),
                                    "FACILITY",
                                    notificationTitle,
                                    notificationContent,
                                    "facility",
                                    String.valueOf(reservationNo)
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    // 알림 발송 실패 시에도 상태 변경은 성공으로 처리
                    logger.error("알림 발송 중 오류 발생: " + e.getMessage());
                }
                
                response.put("success", true);
                response.put("message", "예약 상태가 변경되었습니다.");
                logger.info("예약 상태 변경 성공 - 예약번호: " + reservationNo);
            } else {
                response.put("success", false);
                response.put("message", "예약 상태 변경에 실패했습니다.");
                logger.error("예약 상태 변경 실패 - 예약번호: " + reservationNo);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "예약 상태 변경 중 오류가 발생했습니다.");
            logger.error("예약 상태 변경 중 오류 발생 - 예약번호: " + reservationNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    //--------------------------------
    // 사용자 시설 예약 페이지
    //--------------------------------
    
    /**
     * 사용자 시설 예약 페이지
     */
    @GetMapping("facilityReservation")
    public String facilityReservation(Model model, HttpSession session) {
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            session.setAttribute("alertMsg", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 시설 예약 페이지 접근 시도");
            return "redirect:/";
        }
        
        try {
            // 시설 목록 조회 (활성화된 시설만)
            List<Facility> facilities = facilityService.selectActiveFacilityList();
            model.addAttribute("facilities", facilities);
            
            // 내 예약 목록 조회
            List<Map<String, Object>> myReservations = facilityService.selectMyReservations(loginUser.getEmpNo());
            model.addAttribute("myReservations", myReservations);
            
            // 현재 메뉴 설정
            model.addAttribute("currentMenu", "facility");
            
            logger.info("시설 예약 페이지 접근 - 사용자: " + loginUser.getEmpId());
            return "facility/facilityReservation";
            
        } catch (Exception e) {
            logger.error("시설 예약 페이지 로드 중 오류 발생: " + e.getMessage(), e);
            session.setAttribute("alertMsg", "시설 예약 페이지 로드 중 오류가 발생했습니다.");
            return "redirect:/userMain";
        }
    }
    
    /**
     * 사용 가능한 시설 목록 조회 (AJAX)
     */
    @GetMapping("getAvailableFacilities")
    @ResponseBody
    public Map<String, Object> getAvailableFacilities(
            @RequestParam(value="type", required=false) String type,
            @RequestParam(value="date", required=false) String date,
            @RequestParam(value="keyword", required=false) String keyword,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 시설 목록 조회 시도");
            return response;
        }
        
        try {
        	Map<String, Object> params = new HashMap<>();
            
            if(type != null && !type.isEmpty()) {
                params.put("facilityType", type);
            }
            
            if(date != null && !date.isEmpty()) {
                params.put("date", date);
            }
            
            if(keyword != null && !keyword.trim().isEmpty()) {
                params.put("keyword", keyword.trim());
            }
            
            // 활성화된 시설만 조회
            params.put("status", "ACTIVE");
            
            List<Facility> facilities = facilityService.selectAvailableFacilities(params);
            
            response.put("success", true);
            response.put("facilities", facilities);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "시설 목록 조회 중 오류가 발생했습니다.");
            logger.error("시설 목록 조회 중 오류 발생: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 예약 생성(사용자)
     */
    @PostMapping("createReservation")
    @ResponseBody
    public Map<String, Object> createReservation(
            @RequestParam("facilityNo") int facilityNo,
            @RequestParam("reservationDate") String reservationDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("purpose") String purpose,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 예약 생성 시도");
            return response;
        }
        
        try {
            // 예약 시간 형식 변환: reservationDate(YYYY-MM-DD) + startTime/endTime(HH:mm)
            String startDateTime = reservationDate + " " + startTime + ":00";
            String endDateTime = reservationDate + " " + endTime + ":00";
            
            // 시간 유효성 확인
            if (startTime.compareTo(endTime) >= 0) {
                response.put("success", false);
                response.put("message", "종료 시간은 시작 시간보다 이후여야 합니다.");
                logger.warn("유효하지 않은 시간 범위 - 시작: " + startTime + ", 종료: " + endTime);
                return response;
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("facilityNo", facilityNo);
            params.put("reserverEmpNo", loginUser.getEmpNo());
            params.put("startTime", startDateTime);
            params.put("endTime", endDateTime);
            params.put("purpose", purpose);
            params.put("status", "PENDING"); // ACTIVE 대신 PENDING 사용
            
            // 예약 가능 여부 확인 (시간 중복 체크)
            boolean isAvailable = facilityService.checkReservationAvailability(params);
            
            if(!isAvailable) {
                response.put("success", false);
                response.put("message", "선택한 시간에 이미 예약이 있습니다.");
                logger.warn("시간 중복으로 예약 생성 실패 - 사용자: " + loginUser.getEmpId() + ", 시설번호: " + facilityNo);
                return response;
            }
            
            // 예약 생성
            int result = facilityService.insertReservation(params);
            
            // 예약 생성 성공 시 알림 발송
            if(result > 0) {
                // 시설 정보 가져오기
                Facility facility = facilityService.selectFacility(facilityNo);
                
                // 관리자에게 알림 발송
                try {
                    // 관리자는 1명이므로, empNo가 1인 사용자(또는 시스템 관리자)를 고정으로 사용
                    Employee admin = employeeService.selectEmployee(1); // 관리자 사번을 1로 가정
                    
                    if (admin != null) {
                        logger.info("관리자에게 예약 알림 발송 - 사용자: " + loginUser.getEmpId() + ", 시설명: " + facility.getFacilityName());
                        
                        notificationSenderService.sendNotificationToUser(
                            admin.getEmpNo(),
                            admin.getEmpId(),
                            "FACILITY",
                            "[시설 예약 신청]",
                            loginUser.getEmpName() + "님이 " + facility.getFacilityName() + " 예약을 신청했습니다.",
                            "facility",
                            String.valueOf(result) // 생성된 예약 번호를 참조 ID로 사용 (또는 facilityNo)
                        );
                    } else {
                        logger.warn("시스템 관리자 정보를 찾을 수 없습니다. 알림을 발송할 수 없습니다.");
                    }
                } catch (Exception e) {
                    // 알림 발송 실패 시에도 예약은 성공으로 처리
                    logger.error("알림 발송 중 오류 발생: " + e.getMessage());
                }
                
                response.put("success", true);
                response.put("message", "예약 신청이 완료되었습니다. 관리자 승인 후 이용 가능합니다.");
                logger.info("예약 생성 성공 - 사용자: " + loginUser.getEmpId() + ", 시설번호: " + facilityNo);
            } else {
                response.put("success", false);
                response.put("message", "예약 신청 중 오류가 발생했습니다.");
                logger.error("예약 생성 실패 - 사용자: " + loginUser.getEmpId() + ", 시설번호: " + facilityNo);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "예약 신청 중 오류가 발생했습니다: " + e.getMessage());
            logger.error("예약 생성 중 오류 발생 - 사용자: " + loginUser.getEmpId() + ", 시설번호: " + facilityNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 내 예약 목록 조회 (AJAX)
     */
    @GetMapping("getMyReservations")
    @ResponseBody
    public Map<String, Object> getMyReservations(
            @RequestParam(value="status", required=false) String status,
            @RequestParam(value="date", required=false) String date,
            @RequestParam(value="facilityNo", required=false) Integer facilityNo,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 예약 목록 조회 시도");
            return response;
        }
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("empNo", loginUser.getEmpNo());
            
            if(status != null && !status.isEmpty()) {
                params.put("status", status);
            }
            
            if(date != null && !date.isEmpty()) {
                params.put("date", date);
            }
            
            if(facilityNo != null) {
                params.put("facilityNo", facilityNo);
            }
            
            List<Map<String, Object>> reservations = facilityService.selectMyReservations(params);
            
            response.put("success", true);
            response.put("reservations", reservations);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "예약 목록 조회 중 오류가 발생했습니다.");
            logger.error("예약 목록 조회 중 오류 발생: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 예약 수정 (사용자)
     */
    @PostMapping("updateReservation")
    @ResponseBody
    public Map<String, Object> updateReservation(
            @RequestParam("reservationNo") int reservationNo,
            @RequestParam("facilityNo") int facilityNo,
            @RequestParam("reservationDate") String reservationDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("purpose") String purpose,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 예약 수정 시도 - 예약번호: " + reservationNo);
            return response;
        }
        
        try {
            // 예약 정보 조회 (자신의 예약인지 확인)
            Map<String, Object> reservation = facilityService.selectReservationDetail(reservationNo);
            
            if(reservation == null) {
                response.put("success", false);
                response.put("message", "해당 예약 정보를 찾을 수 없습니다.");
                logger.warn("존재하지 않는 예약 수정 시도 - 예약번호: " + reservationNo);
                return response;
            }
            
            // 자신의 예약인지 확인
            int reserverEmpNo = ((Number)reservation.get("RESERVER_EMP_NO")).intValue();
            if(reserverEmpNo != loginUser.getEmpNo()) {
                response.put("success", false);
                response.put("message", "본인의 예약만 수정할 수 있습니다.");
                logger.warn("타인의 예약 수정 시도 - 예약번호: " + reservationNo);
                return response;
            }
            
            // 예약 상태 확인 (승인 대기 또는 승인 완료 상태만 수정 가능)
            String resStatus = (String)reservation.get("RES_STATUS");
            if(!"PENDING".equals(resStatus) && !"APPROVED".equals(resStatus) && !"ACTIVE".equals(resStatus)) {
                response.put("success", false);
                response.put("message", "수정할 수 없는 예약입니다.");
                logger.warn("수정 불가능한 상태의 예약 수정 시도 - 예약번호: " + reservationNo + ", 상태: " + resStatus);
                return response;
            }
            
            // 예약 시간 형식 변환
            String startDateTime = reservationDate + " " + startTime + ":00";
            String endDateTime = reservationDate + " " + endTime + ":00";
            
            // 시간 유효성 확인
            if (startTime.compareTo(endTime) >= 0) {
                response.put("success", false);
                response.put("message", "종료 시간은 시작 시간보다 이후여야 합니다.");
                logger.warn("유효하지 않은 시간 범위 - 시작: " + startTime + ", 종료: " + endTime);
                return response;
            }
            
            // 중요: 예약 상태 체크 및 처리 (ACTIVE 상태면 PENDING으로 변경)
            boolean isPending = "PENDING".equals(resStatus) || 
                               ("ACTIVE".equals(resStatus) && reservation.get("UPDATE_DATE") == null) ||
                               ("ACTIVE".equals(resStatus) && 
                                  ((Date)reservation.get("CREATE_DATE")).getTime() == 
                                  ((Date)reservation.get("UPDATE_DATE")).getTime());
            
            Map<String, Object> params = new HashMap<>();
            params.put("reservationNo", reservationNo);
            params.put("facilityNo", facilityNo);
            params.put("reserverEmpNo", loginUser.getEmpNo());
            params.put("startTime", startDateTime);
            params.put("endTime", endDateTime);
            params.put("purpose", purpose);
            params.put("excludeReservationNo", reservationNo); // 자신의 예약은 제외
            params.put("resetApprovalStatus", isPending); // 승인 대기 상태였다면 다시 승인 대기로 설정
            
            // 다른 예약과 시간 중복 체크 (자신의 예약은 제외)
            boolean isAvailable = facilityService.checkReservationAvailabilityExcludeSelf(params);
            
            if(!isAvailable) {
                response.put("success", false);
                response.put("message", "선택한 시간에 이미 다른 예약이 있습니다.");
                logger.warn("시간 중복으로 예약 수정 실패 - 예약번호: " + reservationNo);
                return response;
            }
            
            // 예약 수정
            int result = facilityService.updateReservation(params);
            
            if(result > 0) {
                response.put("success", true);
                response.put("message", "예약이 수정되었습니다." + (isPending ? " 관리자 승인 후 이용 가능합니다." : ""));
                logger.info("예약 수정 성공 - 예약번호: " + reservationNo);
            } else {
                response.put("success", false);
                response.put("message", "예약 수정에 실패했습니다.");
                logger.error("예약 수정 실패 - 예약번호: " + reservationNo);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "예약 수정 중 오류가 발생했습니다: " + e.getMessage());
            logger.error("예약 수정 중 오류 발생 - 예약번호: " + reservationNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
        
    /**
     * 예약 취소 (사용자)
     */
    @PostMapping("cancelReservation")
    @ResponseBody
    public Map<String, Object> cancelReservation(
            @RequestParam("reservationNo") int reservationNo,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        // 로그인 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인 후 이용가능합니다.");
            logger.warn("비로그인 사용자의 예약 취소 시도 - 예약번호: " + reservationNo);
            return response;
        }
        
        try {
            // 예약 정보 조회
            Map<String, Object> reservation = facilityService.selectReservationDetail(reservationNo);
            
            if(reservation == null) {
                response.put("success", false);
                response.put("message", "해당 예약 정보를 찾을 수 없습니다.");
                logger.warn("존재하지 않는 예약 취소 시도 - 예약번호: " + reservationNo);
                return response;
            }
            
            // 자신의 예약인지 확인
            int reserverEmpNo = ((Number)reservation.get("RESERVER_EMP_NO")).intValue();
            if(reserverEmpNo != loginUser.getEmpNo()) {
                response.put("success", false);
                response.put("message", "본인의 예약만 취소할 수 있습니다.");
                logger.warn("타인의 예약 취소 시도 - 예약번호: " + reservationNo);
                return response;
            }
            
            // 예약 상태 확인 (승인 대기 또는 승인 완료 상태만 취소 가능)
            String resStatus = (String)reservation.get("RES_STATUS");
            
            if(!resStatus.equals("PENDING") && !resStatus.equals("APPROVED") && !resStatus.equals("ACTIVE")) {
                response.put("success", false);
                response.put("message", "취소할 수 없는 예약입니다.");
                logger.warn("취소 불가능한 상태의 예약 취소 시도 - 예약번호: " + reservationNo + ", 상태: " + resStatus);
                return response;
            }
            
            // 예약 취소 처리
            Map<String, Object> params = new HashMap<>();
            params.put("reservationNo", reservationNo);
            params.put("status", "CANCELED");
            
            int result = facilityService.updateReservationStatus(params);
            
            if(result > 0) {
                response.put("success", true);
                response.put("message", "예약이 취소되었습니다.");
                logger.info("예약 취소 성공 - 예약번호: " + reservationNo);
            } else {
                response.put("success", false);
                response.put("message", "예약 취소에 실패했습니다.");
                logger.error("예약 취소 실패 - 예약번호: " + reservationNo);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "예약 취소 중 오류가 발생했습니다.");
            logger.error("예약 취소 중 오류 발생 - 예약번호: " + reservationNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 파일 저장 메소드
     */
    private String saveFile(MultipartFile file, HttpSession session) {
        String originName = file.getOriginalFilename();
        
        // 저장할 폴더 경로
        String savePath = session.getServletContext().getRealPath("/resources/uploadFiles/facility/");
        
        // 폴더가 없으면 생성
        File folder = new File(savePath);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        
        // 파일명 변경 (UUID + 원본 확장자)
        String ext = originName.substring(originName.lastIndexOf("."));
        String changeName = UUID.randomUUID().toString().replaceAll("-", "") + ext;
        
        // 파일 저장
        try {
            file.transferTo(new File(savePath + changeName));
            return changeName;
        } catch (Exception e) {
            logger.error("파일 저장 중 오류 발생: " + e.getMessage(), e);
            return null;
        }
    }
 }