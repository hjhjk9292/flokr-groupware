package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.EmployeeRequest;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/employees") // 직원 관련 API의 기본 경로
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}) // 필요시 CORS 설정
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 모든 활성 직원 목록 조회
     */
    @GetMapping // GET /api/employees
    public ResponseEntity<ApiResponse<List<Employee>>> getAllActiveEmployees() {
        try {
            log.info("Request to get all active employees.");
            List<Employee> employees = employeeService.getAllActiveEmployees();
            return ResponseEntity.ok(ApiResponse.success("활성 직원 목록 조회", employees));
        } catch (Exception e) {
            log.error("Error getting all active employees: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 목록 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 직원 번호(PK)로 직원 조회
     */
    @GetMapping("/{empNo}") // GET /api/employees/{empNo}
    public ResponseEntity<ApiResponse<Employee>> getEmployeeByEmpNo(@PathVariable Long empNo) {
        try {
            log.info("Request to get employee by empNo: {}", empNo);
            return employeeService.getEmployeeByEmpNo(empNo)
                    .map(employee -> ResponseEntity.ok(ApiResponse.success("직원 조회", employee)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("직원을 찾을 수 없음", "EMPLOYEE_NOT_FOUND")));
        } catch (Exception e) {
            log.error("Error getting employee by empNo {}: {}", empNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 새로운 직원 생성
     */
    @PostMapping // POST /api/employees
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        try {
            log.info("Request to create employee: {}", request.getEmpName()); // Log empName instead of empId as empId is auto-generated
            Employee createdEmployee = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("직원 생성 성공", createdEmployee));
        } catch (ValidationException e) {
            log.warn("Employee creation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("직원 생성 유효성 검증 실패", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating employee: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 생성 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 기존 직원 정보 수정
     */
    @PutMapping("/{empNo}") // PUT /api/employees/{empNo}
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(@PathVariable Long empNo, @Valid @RequestBody EmployeeRequest request) {
        try {
            log.info("Request to update employee empNo {}: {}", empNo, request.getEmpName()); // Log empName instead of empId
            return employeeService.updateEmployee(empNo, request)
                    .map(updatedEmployee -> ResponseEntity.ok(ApiResponse.success("직원 수정 성공", updatedEmployee)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("직원을 찾을 수 없음", "EMPLOYEE_NOT_FOUND")));
        } catch (ValidationException e) {
            log.warn("Employee update validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("직원 수정 유효성 검증 실패", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating employee empNo {}: {}", empNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 수정 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 직원 삭제(비활성화) (실제 데이터는 유지하고 상태만 'N'으로 변경)
     */
    @DeleteMapping("/{empNo}") // DELETE /api/employees/{empNo}
    public ResponseEntity<ApiResponse<String>> deleteEmployee(@PathVariable Long empNo) {
        try {
            log.info("Request to delete employee empNo: {}", empNo);
            boolean isDeleted = employeeService.deleteEmployee(empNo);
            if (isDeleted) {
                return ResponseEntity.ok(ApiResponse.success("직원 비활성화 성공", "DELETED"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("직원을 찾을 수 없음", "EMPLOYEE_NOT_FOUND"));
            }
        } catch (Exception e) {
            log.error("Error deleting employee empNo {}: {}", empNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 비활성화 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 이름으로 직원 검색
     */
    @GetMapping("/search") // GET /api/employees/search?name=홍길동
    public ResponseEntity<ApiResponse<List<Employee>>> searchEmployeesByName(@RequestParam String name) {
        try {
            log.info("Request to search employees by name: {}", name);
            List<Employee> employees = employeeService.searchEmployeesByName(name);
            return ResponseEntity.ok(ApiResponse.success("직원 검색 성공", employees));
        } catch (Exception e) {
            log.error("Error searching employees by name {}: {}", name, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 검색 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 부서 및 직급으로 직원 조회
     */
    @GetMapping("/filter") // GET /api/employees/filter?deptNo=1&positionNo=3
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartmentAndPosition(
            @RequestParam Long deptNo,
            @RequestParam Long positionNo) {
        try {
            log.info("Request to filter employees by deptNo {} and positionNo {}", deptNo, positionNo);
            List<Employee> employees = employeeService.getEmployeesByDepartmentAndPosition(deptNo, positionNo);
            return ResponseEntity.ok(ApiResponse.success("직원 필터링 조회 성공", employees));
        } catch (Exception e) {
            log.error("Error filtering employees by deptNo {} and positionNo {}: {}", deptNo, positionNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 필터링 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 관리자 대시보드 통계 API - 총 직원 수 조회
     */
    @GetMapping("/stats/total") // GET /api/employees/stats/total
    public ResponseEntity<ApiResponse<Long>> getTotalEmployeeCount() {
        try {
            log.info("Request for total employee count.");
            long count = employeeService.getTotalEmployeeCount();
            return ResponseEntity.ok(ApiResponse.success("총 직원 수 조회 성공", count));
        } catch (Exception e) {
            log.error("Error getting total employee count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("총 직원 수 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 관리자 대시보드 통계 API - 활성 직원 수 조회
     */
    @GetMapping("/stats/active") // GET /api/employees/stats/active
    public ResponseEntity<ApiResponse<Long>> getActiveEmployeeCount() {
        try {
            log.info("Request for active employee count.");
            long count = employeeService.getActiveEmployeeCount();
            return ResponseEntity.ok(ApiResponse.success("활성 직원 수 조회 성공", count));
        } catch (Exception e) {
            log.error("Error getting active employee count: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("활성 직원 수 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 관리자 대시보드 통계 API - 부서별 직원 수 조회
     */
    @GetMapping("/stats/by-dept/{deptNo}") // GET /api/employees/stats/by-dept/{deptNo}
    public ResponseEntity<ApiResponse<Long>> getEmployeeCountByDepartment(@PathVariable Long deptNo) {
        try {
            log.info("Request for employee count by department: {}", deptNo);
            // DepartmentService에서 부서 존재 여부를 확인하고 직원 수를 반환하도록 로직이 이미 있음
            long count = employeeService.getEmployeeCountByDepartment(deptNo);
            return ResponseEntity.ok(ApiResponse.success("부서별 직원 수 조회 성공", count));
        } catch (ValidationException e) { // Service에서 던진 ValidationException을 처리
            log.warn("Employee count by department validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("부서별 직원 수 조회 유효성 검증 실패", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting employee count by department {}: {}", deptNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("부서별 직원 수 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 관리자 대시보드 통계 API - 특정 월 신규 입사자 수 조회
     */
    @GetMapping("/stats/new-hires/monthly") // GET /api/employees/stats/new-hires/monthly?year=2025&month=1
    public ResponseEntity<ApiResponse<Long>> getNewHiresCountMonthly(@RequestParam int year, @RequestParam int month) {
        try {
            log.info("Request for new hires count for {}-{}", year, month);
            long count = employeeService.getNewHiresCountMonthly(year, month);
            return ResponseEntity.ok(ApiResponse.success("월별 신규 입사자 수 조회 성공", count));
        } catch (Exception e) {
            log.error("Error getting new hires count for {}-{}: {}", year, month, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("월별 신규 입사자 수 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }
}