package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // LocalDateTime 임포트 추가
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> { // 기본 키 타입을 String -> Long으로 변경

    // PK로 조회 (EMP_NO 기준)
    Optional<Employee> findByEmpNo(Long empNo);

    // 이메일로 직원 찾기 (로그인용)
    Optional<Employee> findByEmail(String email);

    // EMP_ID로 직원 찾기 (로그인용)
    Optional<Employee> findByEmpId(String empId); // 이 메서드는 empId(String)로 찾는 용도입니다.

    // 활성 상태 직원만 조회
    List<Employee> findByStatus(String status);

    // 부서별 직원 조회
    List<Employee> findByDepartment_DeptNo(Long deptNo);

    // 직급별 직원 조회
    List<Employee> findByPosition_PositionNo(Long positionNo);

    // 이름으로 직원 검색
    List<Employee> findByEmpNameContaining(String name);

    // 부서와 직급으로 직원 조회
    @Query("SELECT e FROM Employee e WHERE e.department.deptNo = :deptNo AND e.position.positionNo = :positionNo")
    List<Employee> findByDepartmentAndPosition(@Param("deptNo") Long deptNo, @Param("positionNo") Long positionNo);

    // 관리자 권한 직원 조회
    List<Employee> findByIsAdmin(String isAdmin);

    // 사번 자동 생성을 위한 마지막 사번 조회 (레거시 로직 참고)
    // 패턴: {부서번호}{연도2자리}{순번3자리} (예: 10225001)
    @Query(value = "SELECT emp_id FROM EMPLOYEE " +
            "WHERE SUBSTRING(emp_id, 1, LENGTH(:deptNo)) = :deptNo " + // 부서번호로 시작
            "AND SUBSTRING(emp_id, LENGTH(:deptNo) + 1, 2) = :yearPrefix " + // 연도2자리 확인
            "ORDER BY emp_id DESC LIMIT 1",
            nativeQuery = true)
    Optional<String> findTopByEmpIdStartingWithDeptNoAndYearPrefix(@Param("deptNo") String deptNo, @Param("yearPrefix") String yearPrefix);

    // 전체 직원 수 조회 (대시보드 통계용)
    long count();

    // 활성 직원 수 조회 (대시보드 통계용)
    long countByStatus(String status);

    // 특정 기간에 입사한 직원 수 조회 (월별 신규 입사자 통계용)
    long countByHireDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}