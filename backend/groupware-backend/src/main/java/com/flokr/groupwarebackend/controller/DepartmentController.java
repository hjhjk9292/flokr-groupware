package com.flokr.groupwarebackend.controller;

import com.flokr.groupwarebackend.dto.ApiResponse;
import com.flokr.groupwarebackend.dto.DepartmentRequest; // DepartmentRequest 임포트 추가
import com.flokr.groupwarebackend.entity.Department;
import com.flokr.groupwarebackend.service.DepartmentService;
import jakarta.validation.Valid; // @Valid 어노테이션을 위해 임포트 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // HttpStatus 임포트 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/departments") // 부서 관련 API의 기본 경로
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}) // 필요시 CORS 설정
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 모든 활성 부서 목록 조회
     */
    @GetMapping // GET /api/departments
    public ResponseEntity<ApiResponse<List<Department>>> getAllActiveDepartments() {
        try {
            log.info("Request to get all active departments.");
            List<Department> departments = departmentService.getAllActiveDepartments();
            return ResponseEntity.ok(ApiResponse.success("활성 부서 목록 조회", departments)); // 메시지 수정
        } catch (Exception e) {
            log.error("Error getting all active departments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("부서 목록 조회 실패", "INTERNAL_SERVER_ERROR")); // 메시지 수정
        }
    }

    /**
     * 부서 번호로 부서 조회
     */
    @GetMapping("/{deptNo}") // GET /api/departments/{deptNo}
    public ResponseEntity<ApiResponse<Department>> getDepartmentByNo(@PathVariable Long deptNo) {
        try {
            log.info("Request to get department by deptNo: {}", deptNo);
            return departmentService.getDepartmentByNo(deptNo)
                    .map(department -> ResponseEntity.ok(ApiResponse.success("부서 조회", department))) // 메시지 수정
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("부서를 찾을 수 없음", "DEPARTMENT_NOT_FOUND"))); // 메시지 수정
        } catch (Exception e) {
            log.error("Error getting department by deptNo {}: {}", deptNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("부서 조회 실패", "INTERNAL_SERVER_ERROR")); // 메시지 수정
        }
    }

    /**
     * 새로운 부서 생성
     */
    @PostMapping // POST /api/departments
    public ResponseEntity<ApiResponse<Department>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        try {
            log.info("Request to create department: {}", request.getDeptName());
            Department createdDepartment = departmentService.createDepartment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("부서 생성 성공", createdDepartment)); // 메시지 수정
        } catch (Exception e) {
            log.error("Error creating department: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("부서 생성 실패", "INTERNAL_SERVER_ERROR")); // 메시지 수정
        }
    }

    /**
     * 기존 부서 정보 수정
     */
    @PutMapping("/{deptNo}") // PUT /api/departments/{deptNo}
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable Long deptNo, @Valid @RequestBody DepartmentRequest request) {
        try {
            log.info("Request to update department deptNo {}: {}", deptNo, request.getDeptName());
            return departmentService.updateDepartment(deptNo, request)
                    .map(updatedDepartment -> ResponseEntity.ok(ApiResponse.success("부서 수정 성공", updatedDepartment))) // 메시지 수정
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("부서를 찾을 수 없음", "DEPARTMENT_NOT_FOUND"))); // 메시지 수정
        } catch (Exception e) {
            log.error("Error updating department deptNo {}: {}", deptNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("부서 수정 실패", "INTERNAL_SERVER_ERROR")); // 메시지 수정
        }
    }

    /**
     * 부서 삭제(비활성화)
     */
    @DeleteMapping("/{deptNo}") // DELETE /api/departments/{deptNo}
    public ResponseEntity<ApiResponse<String>> deleteDepartment(@PathVariable Long deptNo) {
        try {
            log.info("Request to delete department deptNo: {}", deptNo);
            boolean isDeleted = departmentService.deleteDepartment(deptNo);
            if (isDeleted) {
                return ResponseEntity.ok(ApiResponse.success("부서 비활성화 성공", "DELETED")); // 메시지 수정
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("부서를 찾을 수 없음", "DEPARTMENT_NOT_FOUND")); // 메시지 수정
            }
        } catch (Exception e) {
            log.error("Error deleting department deptNo {}: {}", deptNo, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(ApiResponse.error("부서 비활성화 실패", "INTERNAL_SERVER_ERROR")); // 메시지 수정
        }
    }
}