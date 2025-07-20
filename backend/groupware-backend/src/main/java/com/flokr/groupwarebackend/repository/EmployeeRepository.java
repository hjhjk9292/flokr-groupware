package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    
    // 이메일로 직원 찾기 (로그인용)
    Optional<Employee> findByEmail(String email);
    
    // 활성 상태 직원만 조회
    List<Employee> findByStatus(Employee.EmployeeStatus status);
    
    // 부서별 직원 조회
    List<Employee> findByDepartment_DeptId(Long deptId);
    
    // 직급별 직원 조회
    List<Employee> findByPosition_PositionId(Long positionId);
    
    // 이름으로 직원 검색
    List<Employee> findByEmpNameContaining(String name);
    
    // 부서와 직급으로 직원 조회
    @Query("SELECT e FROM Employee e WHERE e.department.deptId = :deptId AND e.position.positionId = :positionId")
    List<Employee> findByDepartmentAndPosition(@Param("deptId") Long deptId, @Param("positionId") Long positionId);
    
    // 관리자 권한 직원 조회
    List<Employee> findByRole(Employee.EmployeeRole role);
}
