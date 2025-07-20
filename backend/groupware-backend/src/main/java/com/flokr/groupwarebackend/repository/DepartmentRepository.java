package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 부서명으로 조회
    Optional<Department> findByDeptName(String deptName);

    // 활성 상태 부서만 조회
    List<Department> findByStatusOrderByDeptNameAsc(String status);

    // 부서명으로 검색
    List<Department> findByDeptNameContaining(String keyword);

    // 부서별 직원 수 조회
    @Query("SELECT d.deptName, COUNT(e) FROM Department d LEFT JOIN d.employees e WHERE d.status = 'Y' GROUP BY d.deptNo, d.deptName ORDER BY d.deptName")
    List<Object[]> findDepartmentEmployeeCounts();

    // 모든 활성 부서 조회 (생성일순)
    List<Department> findByStatusOrderByCreateDateAsc(String status);
}