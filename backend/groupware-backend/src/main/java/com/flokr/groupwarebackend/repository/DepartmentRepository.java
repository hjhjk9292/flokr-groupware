package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    // 부서명으로 조회
    Optional<Department> findByDeptName(String deptName);
    
    // 부서 코드로 조회
    Optional<Department> findByDeptCode(String deptCode);
    
    // 상위 부서별 하위 부서 조회
    List<Department> findByParentDeptId(Long parentDeptId);
    
    // 최상위 부서 조회 (parentDeptId가 null인 부서)
    List<Department> findByParentDeptIdIsNull();
    
    // 부서 계층 구조 조회 (재귀 쿼리)
    @Query(value = "WITH RECURSIVE dept_hierarchy AS (" +
                   "  SELECT dept_id, dept_name, dept_code, parent_dept_id, 0 as level " +
                   "  FROM departments WHERE parent_dept_id IS NULL " +
                   "  UNION ALL " +
                   "  SELECT d.dept_id, d.dept_name, d.dept_code, d.parent_dept_id, dh.level + 1 " +
                   "  FROM departments d JOIN dept_hierarchy dh ON d.parent_dept_id = dh.dept_id" +
                   ") SELECT * FROM dept_hierarchy ORDER BY level, dept_name", 
           nativeQuery = true)
    List<Object[]> findDepartmentHierarchy();
    
    // 특정 관리자가 관리하는 부서 조회
    List<Department> findByManager_EmpId(String managerId);
}
