package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.dto.DepartmentRequest;
import com.flokr.groupwarebackend.entity.Department;
import com.flokr.groupwarebackend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    /**
     * 모든 활성 부서 목록 조회
     * @return 활성 상태인 부서 목록
     */
    @Transactional(readOnly = true)
    public List<Department> getAllActiveDepartments() {
        return departmentRepository.findByStatusOrderByDeptNameAsc("Y"); // 상태가 'Y'인 부서만 이름 순으로 조회
    }

    /**
     * 부서 번호로 부서 조회
     * @param deptNo 부서 번호
     * @return 해당 부서 엔티티 (존재하지 않으면 Optional.empty)
     */
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentByNo(Long deptNo) {
        return departmentRepository.findById(deptNo);
    }

    /**
     * 새로운 부서 생성
     * @param request 부서 생성 요청 DTO
     * @return 생성된 부서 엔티티
     */
    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        Department newDepartment = new Department();
        newDepartment.setDeptName(request.getDeptName());
        // status가 요청에 포함되어 있다면 설정, 아니면 엔티티의 @Builder.Default "Y" 사용
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            newDepartment.setStatus(request.getStatus());
        } else {
            newDepartment.setStatus("Y"); // 기본값 설정
        }
        newDepartment.setCreateDate(LocalDateTime.now()); // 수동 설정 또는 CreationTimestamp에 맡김
        return departmentRepository.save(newDepartment);
    }

    /**
     * 기존 부서 정보 수정
     * @param deptNo 수정할 부서 번호
     * @param request 부서 수정 요청 DTO
     * @return 수정된 부서 엔티티 (부서가 존재하지 않으면 Optional.empty)
     */
    @Transactional
    public Optional<Department> updateDepartment(Long deptNo, DepartmentRequest request) {
        return departmentRepository.findById(deptNo).map(department -> {
            department.setDeptName(request.getDeptName());
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                department.setStatus(request.getStatus());
            }
            // updateDate는 @PreUpdate를 통해 Employee 엔티티에서 자동 처리되지만, Department에는 없음. 필요시 추가
            return departmentRepository.save(department);
        });
    }

    /**
     * 부서 삭제(비활성화) (실제 데이터는 유지하고 상태만 'N'으로 변경)
     * @param deptNo 삭제할 부서 번호
     * @return 삭제 성공 여부 (true: 성공, false: 부서 없음)
     */
    @Transactional
    public boolean deleteDepartment(Long deptNo) {
        return departmentRepository.findById(deptNo).map(department -> {
            department.setStatus("N"); // 상태를 'N'으로 변경
            departmentRepository.save(department); // 변경된 상태 저장
            return true;
        }).orElse(false);
    }

    /**
     * 부서별 직원 수 조회
     * @return 부서명과 해당 부서의 직원 수를 담은 Object 배열 리스트
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDepartmentEmployeeCounts() {
        return departmentRepository.findDepartmentEmployeeCounts();
    }
}