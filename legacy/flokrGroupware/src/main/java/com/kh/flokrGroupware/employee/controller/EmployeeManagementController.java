package com.kh.flokrGroupware.employee.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.flokrGroupware.employee.model.service.EmployeeService;
import com.kh.flokrGroupware.employee.model.vo.Department;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.employee.model.vo.Position;

@Controller
public class EmployeeManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeManagementController.class);
    
    @Autowired
    private EmployeeService employeeService; // 기존 서비스 재사용
    
    @Autowired
    private BCryptPasswordEncoder bcryptPasswordEncoder;
    
    // ----- 접속 사용자 관리 영역 -----
    
    /**
     * 접속 사용자 관리 페이지
     */
    @GetMapping("adminOnlineEmployee")
    public String onlineEmployees(Model model, HttpSession session) {
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 접속 사용자 관리 페이지 접근 시도");
            return "redirect:/";
        }
        
        // 부서 목록 조회 (기존 서비스 메서드 활용)
        ArrayList<Department> departments = employeeService.selectDepartmentList();
        model.addAttribute("departments", departments);
        
        // 최근 접속 로그 조회 (최근 5개)
        List<Map<String, Object>> recentLogs = getRecentLogs(5);
        model.addAttribute("recentLogs", recentLogs);
        
        logger.info("접속 사용자 관리 페이지 접근 - 관리자: " + loginUser.getEmpId());
        return "employee/adminOnlineEmployee";
    }
    
    /**
     * 접속 중인 사용자 목록 조회 (AJAX)
     */
    @GetMapping("getOnlineEmployee")
    @ResponseBody
    public List<Map<String, Object>> getOnlineEmployees(HttpSession session) {
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            logger.warn("권한 없는 사용자의 접속 사용자 목록 조회 시도");
            return new ArrayList<>();
        }
        
        // 세션에서 현재 접속 중인 사용자 조회 (MVP에서는 임시 데이터 사용)
        List<Map<String, Object>> onlineEmployees = new ArrayList<>();
        
        // 임시 데이터 생성 (실제로는 세션 관리를 통해 접속 중인 사용자를 조회)
        try {
            // 임시 데이터 생성 (테스트용)
            Map<String, Object> emp1 = new HashMap<>();
            emp1.put("empId", "125002");
            emp1.put("empName", "정사원");
            emp1.put("deptName", "인사팀");
            emp1.put("positionName", "사원");
            emp1.put("status", "online");
            emp1.put("lastLoginTime", new Date());
            emp1.put("ipAddress", "192.168.1.101");
            onlineEmployees.add(emp1);
            
            Map<String, Object> emp2 = new HashMap<>();
            emp2.put("empId", "admin");
            emp2.put("empName", "관리자");
            emp2.put("deptName", "인사팀");
            emp2.put("positionName", "과장");
            emp2.put("status", "online");
            emp2.put("lastLoginTime", new Date());
            emp2.put("ipAddress", "192.168.1.100");
            onlineEmployees.add(emp2);
            
            Map<String, Object> emp3 = new HashMap<>();
            emp3.put("empId", "103001");
            emp3.put("empName", "김개발");
            emp3.put("deptName", "개발팀");
            emp3.put("positionName", "대리");
            emp3.put("status", "online");
            emp3.put("lastLoginTime", new Date());
            emp3.put("ipAddress", "192.168.1.105");
            onlineEmployees.add(emp3);
            
            logger.info("접속 사용자 목록 조회 - 접속자 수: " + onlineEmployees.size());
        } catch (Exception e) {
            logger.error("접속 사용자 목록 조회 중 오류 발생: " + e.getMessage(), e);
        }
        
        return onlineEmployees;
    }
    
    /**
     * 사용자 강제 로그아웃 처리
     */
    @PostMapping("logoutEmployee")
    @ResponseBody
    public Map<String, Object> logoutEmployee(@RequestParam("empId") String empId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 강제 로그아웃 시도 - 대상 ID: " + empId);
            return response;
        }
        
        try {
            // 실제 구현에서는 서비스 호출
            // MVP에서는 임시 성공 처리
            boolean success = true;
            
            // 로그 기록 - 실제 구현에서는 DB에 저장
            if(success) {
                Map<String, Object> logEntry = new HashMap<>();
                logEntry.put("empId", empId);
                logEntry.put("actionType", "logout");
                logEntry.put("description", "관리자 " + loginUser.getEmpId() + "에 의한 강제 로그아웃");
                logEntry.put("ipAddress", "Unknown");
                logEntry.put("actionTime", new Date());
                
                // 실제 구현에서는 로그를 DB에 저장
                // logService.insertLog(logEntry);
                
                response.put("success", true);
                response.put("message", "사용자가 로그아웃 되었습니다.");
                logger.info("사용자 강제 로그아웃 성공 - ID: " + empId);
            } else {
                response.put("success", false);
                response.put("message", "사용자 로그아웃에 실패했습니다.");
                logger.error("사용자 강제 로그아웃 실패 - ID: " + empId);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 로그아웃 중 오류 발생: " + e.getMessage());
            logger.error("사용자 강제 로그아웃 중 오류 발생 - ID: " + empId + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 최근 접속 로그 조회 (임시 구현 - MVP용)
     */
    private List<Map<String, Object>> getRecentLogs(int limit) {
        List<Map<String, Object>> logs = new ArrayList<>();
        
        // 임시 로그 데이터 생성
        Map<String, Object> log1 = new HashMap<>();
        log1.put("empId", "125002");
        log1.put("empName", "정사원");
        log1.put("actionType", "login");
        log1.put("description", "로그인 성공");
        log1.put("ipAddress", "192.168.1.101");
        log1.put("actionTime", new Date());
        logs.add(log1);
        
        Map<String, Object> log2 = new HashMap<>();
        log2.put("empId", "admin");
        log2.put("empName", "관리자");
        log2.put("actionType", "login");
        log2.put("description", "로그인 성공");
        log2.put("ipAddress", "192.168.1.100");
        log2.put("actionTime", new Date());
        logs.add(log2);
        
        Map<String, Object> log3 = new HashMap<>();
        log3.put("empId", "104005");
        log3.put("empName", "김디자인");
        log3.put("actionType", "logout");
        log3.put("description", "사용자에 의한 로그아웃");
        log3.put("ipAddress", "192.168.1.102");
        log3.put("actionTime", new Date());
        logs.add(log3);
        
        Map<String, Object> log4 = new HashMap<>();
        log4.put("empId", "103001");
        log4.put("empName", "김개발");
        log4.put("actionType", "login");
        log4.put("description", "로그인 성공");
        log4.put("ipAddress", "192.168.1.105");
        log4.put("actionTime", new Date());
        logs.add(log4);
        
        Map<String, Object> log5 = new HashMap<>();
        log5.put("empId", "101003");
        log5.put("empName", "이경영");
        log5.put("actionType", "logout");
        log5.put("description", "세션 만료로 인한 자동 로그아웃");
        log5.put("ipAddress", "192.168.1.103");
        log5.put("actionTime", new Date());
        logs.add(log5);
        
        return logs.subList(0, Math.min(limit, logs.size()));
    }
    
    // ----- 사용자 정보 관리 영역 -----
    
    /**
     * 사용자 정보 관리 페이지
     */
    @GetMapping("adminEmployeeManagement")
    public String employeeManagement(Model model, HttpSession session) {
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 사용자 정보 관리 페이지 접근 시도");
            return "redirect:/";
        }
        
        // 부서 및 직급 목록 조회
        ArrayList<Department> departments = employeeService.selectDepartmentList();
        ArrayList<Position> positions = employeeService.selectPositionList();
        
        model.addAttribute("departments", departments);
        model.addAttribute("positions", positions);
        
        logger.info("사용자 정보 관리 페이지 접근 - 관리자: " + loginUser.getEmpId());
        return "employee/adminEmployeeManagement";
    }
    
    /**
     * 모든 사용자 목록 조회 (AJAX) - 임시 데이터 사용
     */
    @GetMapping("getAllEmployees")
    @ResponseBody
    public List<Employee> getAllEmployees(HttpSession session) {
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            logger.warn("권한 없는 사용자의 전체 사용자 목록 조회 시도");
            return new ArrayList<>();
        }
        
        try {
            // 임시 데이터 생성 (MVP 구현)
            ArrayList<Employee> employees = new ArrayList<>();
            
            // 사용자 1
            Employee emp1 = new Employee();
            emp1.setEmpNo(1);
            emp1.setEmpId("125002");
            emp1.setEmpName("정사원");
            emp1.setDeptNo(102);
            emp1.setDeptName("인사팀");
            emp1.setPositionNo(1);
            emp1.setPositionName("사원");
            emp1.setEmail("125002@flokr.com");
            emp1.setPhone("010-1234-5678");
            emp1.setHireDate(java.sql.Date.valueOf("2020-06-15"));
            emp1.setStatus("Y");
            emp1.setIsAdmin("N");
            employees.add(emp1);
            
            // 사용자 2 (관리자)
            Employee emp2 = new Employee();
            emp2.setEmpNo(2);
            emp2.setEmpId("admin");
            emp2.setEmpName("관리자");
            emp2.setDeptNo(102);
            emp2.setDeptName("인사팀");
            emp2.setPositionNo(3);
            emp2.setPositionName("과장");
            emp2.setEmail("admin@flokr.com");
            emp2.setPhone("010-9876-5432");
            emp2.setHireDate(java.sql.Date.valueOf("2019-01-10"));
            emp2.setStatus("Y");
            emp2.setIsAdmin("Y");
            employees.add(emp2);
            
            // 사용자 3
            Employee emp3 = new Employee();
            emp3.setEmpNo(3);
            emp3.setEmpId("103001");
            emp3.setEmpName("김개발");
            emp3.setDeptNo(103);
            emp3.setDeptName("개발팀");
            emp3.setPositionNo(2);
            emp3.setPositionName("대리");
            emp3.setEmail("103001@flokr.com");
            emp3.setPhone("010-2222-3333");
            emp3.setHireDate(java.sql.Date.valueOf("2021-03-22"));
            emp3.setStatus("Y");
            emp3.setIsAdmin("N");
            employees.add(emp3);
            
            logger.info("전체 사용자 목록 조회 - 사용자 수: " + employees.size());
            return employees;
            
        } catch (Exception e) {
            logger.error("전체 사용자 목록 조회 중 오류 발생: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 사용자 상세 정보 조회 (AJAX) - 임시 데이터 사용
     */
    @GetMapping("getUserDetail")
    @ResponseBody
    public Map<String, Object> getUserDetail(@RequestParam("empNo") int empNo, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            logger.warn("권한 없는 사용자의 사용자 상세 정보 조회 시도 - 사원번호: " + empNo);
            return response; // 빈 응답 반환
        }
        
        try {
            // 임시 데이터 - 사용자 목록에서 사원번호로 검색
            Employee employee = null;
            List<Employee> employees = getAllEmployees(session);
            
            for(Employee emp : employees) {
                if(emp.getEmpNo() == empNo) {
                    employee = emp;
                    break;
                }
            }
            
            if(employee != null) {
                // 응답 데이터에 사용자 정보 추가
                response.put("empNo", employee.getEmpNo());
                response.put("empId", employee.getEmpId());
                response.put("empName", employee.getEmpName());
                response.put("email", employee.getEmail());
                response.put("phone", employee.getPhone());
                response.put("deptNo", employee.getDeptNo());
                response.put("deptName", employee.getDeptName());
                response.put("positionNo", employee.getPositionNo());
                response.put("positionName", employee.getPositionName());
                response.put("hireDate", employee.getHireDate());
                response.put("isAdmin", employee.getIsAdmin());
                response.put("status", employee.getStatus());
                
                logger.info("사용자 상세 정보 조회 성공 - 사원번호: " + empNo + ", 이름: " + employee.getEmpName());
            } else {
                logger.warn("존재하지 않는 사용자 정보 조회 시도 - 사원번호: " + empNo);
            }
        } catch (Exception e) {
            logger.error("사용자 상세 정보 조회 중 오류 발생 - 사원번호: " + empNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 사용자 정보 업데이트
     */
    @PostMapping("updateUserInfo")
    @ResponseBody
    public Map<String, Object> updateUserInfo(Employee employee, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 사용자 정보 수정 시도 - 사원번호: " + employee.getEmpNo());
            return response;
        }
        
        try {
            // 사용자 정보 업데이트
            int result = employeeService.updateEmployee(employee);
            
            if(result > 0) {
                response.put("success", true);
                response.put("message", "사용자 정보가 수정되었습니다.");
                logger.info("사용자 정보 수정 성공 - 사원번호: " + employee.getEmpNo() + ", 이름: " + employee.getEmpName());
            } else {
                response.put("success", false);
                response.put("message", "사용자 정보 수정에 실패했습니다.");
                logger.error("사용자 정보 수정 실패 - 사원번호: " + employee.getEmpNo() + ", 이름: " + employee.getEmpName());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 정보 수정 중 오류 발생: " + e.getMessage());
            logger.error("사용자 정보 수정 중 오류 발생 - 사원번호: " + employee.getEmpNo() + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
    /**
     * 비밀번호 초기화
     */
    @PostMapping("resetUserPassword")
    @ResponseBody
    public Map<String, Object> resetUserPassword(@RequestParam("empNo") int empNo, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            response.put("success", false);
            response.put("message", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 비밀번호 초기화 시도 - 사원번호: " + empNo);
            return response;
        }
        
        try {
            // 사용자 정보 조회
            Employee employee = employeeService.selectEmployee(empNo);
            
            if(employee != null) {
                // 사번 + "init"로 초기 비밀번호 설정
                String initialPassword = employee.getEmpId() + "init";
                
                // 비밀번호 암호화
                String encryptedPassword = bcryptPasswordEncoder.encode(initialPassword);
                
                // 비밀번호 업데이트 파라미터 설정
                Map<String, Object> params = new HashMap<>();
                params.put("empNo", empNo);
                params.put("passwordHash", encryptedPassword);
                
                int result = employeeService.resetPassword(params);
                
                if(result > 0) {
                    response.put("success", true);
                    response.put("message", "비밀번호가 초기화되었습니다. 초기 비밀번호는 '" + initialPassword + "'입니다.");
                    logger.info("비밀번호 초기화 성공 - 사원번호: " + empNo + ", 이름: " + employee.getEmpName());
                } else {
                    response.put("success", false);
                    response.put("message", "비밀번호 초기화에 실패했습니다.");
                    logger.error("비밀번호 초기화 실패 - 사원번호: " + empNo);
                }
            } else {
                response.put("success", false);
                response.put("message", "사용자 정보를 찾을 수 없습니다.");
                logger.warn("존재하지 않는 사용자의 비밀번호 초기화 시도 - 사원번호: " + empNo);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 초기화 중 오류 발생: " + e.getMessage());
            logger.error("비밀번호 초기화 중 오류 발생 - 사원번호: " + empNo + ", 오류: " + e.getMessage(), e);
        }
        
        return response;
    }
    
}