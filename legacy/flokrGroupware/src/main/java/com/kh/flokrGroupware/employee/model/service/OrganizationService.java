package com.kh.flokrGroupware.employee.model.service;

import java.util.ArrayList;

import com.kh.flokrGroupware.employee.model.vo.Department;
import com.kh.flokrGroupware.employee.model.vo.Employee;

public interface OrganizationService {
    
    /**
     * 부서 목록 조회
     * @return 부서 목록
     */
    ArrayList<Department> selectDepartmentList();
    
    /**
     * 부서 정보 조회
     * @param deptNo 부서 번호
     * @return 부서 정보
     */
    Department selectDepartment(int deptNo);
    
    /**
     * 부서 등록
     * @param dept 등록할 부서 정보
     * @return 처리 결과 (1: 성공, 0: 실패)
     */
    int insertDepartment(Department dept);
    
    /**
     * 부서 정보 수정
     * @param dept 수정할 부서 정보
     * @return 처리 결과 (1: 성공, 0: 실패)
     */
    int updateDepartment(Department dept);
    
    /**
     * 부서 삭제 (상태 변경)
     * @param deptNo 삭제할 부서 번호
     * @return 처리 결과 (1: 성공, 0: 실패)
     */
    int deleteDepartment(int deptNo);
    
    /**
     * 부서별 직원 목록 조회
     * @param deptNo 부서 번호
     * @return 해당 부서 직원 목록
     */
    ArrayList<Employee> selectEmployeesByDept(int deptNo);
    
    /**
     * 직원 상세 정보 조회
     * @param empNo 직원 번호
     * @return 직원 정보
     */
    Employee selectEmployee(int empNo);
}