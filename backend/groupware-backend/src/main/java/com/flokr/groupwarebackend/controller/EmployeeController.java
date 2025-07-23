package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.EmployeeRequest;
import com.flokr.groupwarebackend.dto.EmployeeUpdateRequest;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesWithFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String empId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long deptNo,
            @RequestParam(defaultValue = "1") int currentPage) {
        try {
            log.info("Request to get employees with filters - name: {}, empId: {}, email: {}, status: {}, deptNo: {}",
                    name, empId, email, status, deptNo);

            List<Employee> employees;

            if (name != null && !name.trim().isEmpty()) {
                employees = employeeService.searchEmployeesByName(name.trim());
                log.info("Search by name '{}' returned {} employees", name.trim(), employees.size());
            } else if (empId != null && !empId.trim().isEmpty()) {
                employees = employeeService.getEmployeeByEmpId(empId.trim())
                        .map(Arrays::asList)
                        .orElse(Collections.emptyList());
                log.info("Search by empId '{}' returned {} employees", empId.trim(), employees.size());
            } else if (email != null && !email.trim().isEmpty()) {
                employees = employeeService.getEmployeeByEmail(email.trim())
                        .map(Arrays::asList)
                        .orElse(Collections.emptyList());
                log.info("Search by email '{}' returned {} employees", email.trim(), employees.size());
            } else if (deptNo != null) {
                employees = employeeService.getEmployeesByDepartmentAndPosition(deptNo, null);
                log.info("Search by deptNo '{}' returned {} employees", deptNo, employees.size());
            } else {
                employees = employeeService.getAllActiveEmployees();
                log.info("Retrieved all active employees: {} employees", employees.size());
            }

            if (status != null && !status.trim().isEmpty()) {
                employees = employees.stream()
                        .filter(emp -> status.equals(emp.getStatus()))
                        .collect(Collectors.toList());
                log.info("After status filter '{}': {} employees", status, employees.size());
            } else {
                employees = employees.stream()
                        .filter(emp -> "Y".equals(emp.getStatus()))
                        .collect(Collectors.toList());
                log.info("Applied default active filter: {} employees", employees.size());
            }

            return ResponseEntity.ok(ApiResponse.success("직원 목록 조회 성공", employees));
        } catch (Exception e) {
            log.error("Error getting employees with filters: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("직원 목록 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/{empNo}")
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

    @PostMapping
    public ResponseEntity<ApiResponse<Employee>> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        try {
            log.info("Request to create employee: {}", request.getEmpName());
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

    // 🔥 이 메소드만 수정됨!
    @PutMapping("/{empNo}")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(@PathVariable Long empNo, @Valid @RequestBody EmployeeUpdateRequest request) {
        try {
            log.info("Request to update employee empNo {}: {}", empNo, request.getEmpName());
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

    @DeleteMapping("/{empNo}")
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

    @GetMapping("/search")
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

    @GetMapping("/filter")
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

    @GetMapping("/stats/total")
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

    @GetMapping("/stats/active")
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

    @GetMapping("/stats/by-dept/{deptNo}")
    public ResponseEntity<ApiResponse<Long>> getEmployeeCountByDepartment(@PathVariable Long deptNo) {
        try {
            log.info("Request for employee count by department: {}", deptNo);
            long count = employeeService.getEmployeeCountByDepartment(deptNo);
            return ResponseEntity.ok(ApiResponse.success("부서별 직원 수 조회 성공", count));
        } catch (ValidationException e) {
            log.warn("Employee count by department validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("부서별 직원 수 조회 유효성 검증 실패", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting employee count by department {}: {}", deptNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("부서별 직원 수 조회 실패", "INTERNAL_SERVER_ERROR"));
        }
    }

    @GetMapping("/stats/new-hires/monthly")
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