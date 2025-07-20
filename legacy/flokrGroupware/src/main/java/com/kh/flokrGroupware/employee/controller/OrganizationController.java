package com.kh.flokrGroupware.employee.controller;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.flokrGroupware.employee.model.service.OrganizationService;
import com.kh.flokrGroupware.employee.model.vo.Department;
import com.kh.flokrGroupware.employee.model.vo.Employee;

@Controller
public class OrganizationController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);
    
    @Autowired
    private OrganizationService organizationService;
    
    // 조직도 관리 페이지 표시
    @GetMapping("adminOrganization")
    public String organizationManagement(Model model, HttpSession session) {
        // 로그인 여부 및 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 조직도 관리 페이지 접근 시도");
            return "redirect:/";
        }
        
        // 부서 목록 조회
        ArrayList<Department> departmentList = organizationService.selectDepartmentList();
        
        model.addAttribute("departmentList", departmentList);
        
        logger.info("조직도 관리 페이지 접근 - 관리자: " + loginUser.getEmpId());
        return "employee/adminOrganization";
    }
    
    // 부서 등록 처리
    @PostMapping("insertDepartment")
    public String insertDepartment(Department dept, 
                                HttpSession session, 
                                RedirectAttributes ra) {
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 부서 등록 시도");
            return "redirect:/";
        }
        
        logger.info("부서 등록 시도 - 부서명: " + dept.getDeptName());
        
        // 부서 등록 서비스 호출
        int result = organizationService.insertDepartment(dept);
        
        if(result > 0) {
            ra.addFlashAttribute("alertMsg", "부서가 등록되었습니다.");
            logger.info("부서 등록 성공 - 부서명: " + dept.getDeptName());
        } else {
            ra.addFlashAttribute("alertMsg", "부서 등록에 실패했습니다.");
            logger.error("부서 등록 실패 - 부서명: " + dept.getDeptName());
        }
        
        return "redirect:/adminOrganization";
    }
    
    // 부서 정보 수정 처리
    @PostMapping("updateDepartment")
    public String updateDepartment(Department dept, 
                                HttpSession session, 
                                RedirectAttributes ra) {
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 부서 수정 시도");
            return "redirect:/";
        }
        
        logger.info("부서 수정 시도 - 부서번호: " + dept.getDeptNo() + ", 부서명: " + dept.getDeptName());
        
        // 부서 수정 서비스 호출
        int result = organizationService.updateDepartment(dept);
        
        if(result > 0) {
            ra.addFlashAttribute("alertMsg", "부서 정보가 수정되었습니다.");
            logger.info("부서 수정 성공 - 부서번호: " + dept.getDeptNo() + ", 부서명: " + dept.getDeptName());
        } else {
            ra.addFlashAttribute("alertMsg", "부서 수정에 실패했습니다.");
            logger.error("부서 수정 실패 - 부서번호: " + dept.getDeptNo() + ", 부서명: " + dept.getDeptName());
        }
        
        return "redirect:/adminOrganization";
    }
    
    // 부서 삭제 처리
    @PostMapping("deleteDepartment")
    public String deleteDepartment(@RequestParam("deptNo") int deptNo, 
                                HttpSession session, 
                                RedirectAttributes ra) {
        
        // 관리자 권한 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null || !"Y".equals(loginUser.getIsAdmin())) {
            session.setAttribute("alertMsg", "관리자만 접근 가능합니다.");
            logger.warn("권한 없는 사용자의 부서 삭제 시도");
            return "redirect:/";
        }
        
        logger.info("부서 삭제 시도 - 부서번호: " + deptNo);
        
        // 부서 삭제 서비스 호출
        int result = organizationService.deleteDepartment(deptNo);
        
        if(result > 0) {
            ra.addFlashAttribute("alertMsg", "부서가 삭제되었습니다.");
            logger.info("부서 삭제 성공 - 부서번호: " + deptNo);
        } else {
            ra.addFlashAttribute("alertMsg", "부서 삭제에 실패했습니다.");
            logger.error("부서 삭제 실패 - 부서번호: " + deptNo);
        }
        
        return "redirect:/adminOrganization";
    }
    
    // 부서별 직원 목록 조회 (AJAX)
    @GetMapping("getEmployeesByDept")
    @ResponseBody
    public List<Employee> getEmployeesByDept(@RequestParam("deptNo") int deptNo, HttpSession session) {
        // 로그인 여부 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            logger.warn("비로그인 사용자의 부서별 직원 목록 조회 시도");
            return new ArrayList<>();
        }
        
        logger.info("부서별 직원 목록 조회 - 부서번호: " + deptNo);
        
        // 부서별 직원 목록 조회 서비스 호출
        return organizationService.selectEmployeesByDept(deptNo);
    }
    
    // 직원 상세 정보 조회 (AJAX)
    @GetMapping("getEmployeeInfo")
    @ResponseBody
    public Employee getEmployeeInfo(@RequestParam("empNo") int empNo, HttpSession session) {
        // 로그인 여부 체크
        Employee loginUser = (Employee)session.getAttribute("loginUser");
        if(loginUser == null) {
            logger.warn("비로그인 사용자의 직원 상세 정보 조회 시도");
            return null;
        }
        
        logger.info("직원 상세 정보 조회 - 직원번호: " + empNo);
        
        // 직원 상세 정보 조회 서비스 호출
        return organizationService.selectEmployee(empNo);
    }
}