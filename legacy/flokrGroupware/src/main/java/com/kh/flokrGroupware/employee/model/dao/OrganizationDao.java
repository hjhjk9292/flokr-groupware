package com.kh.flokrGroupware.employee.model.dao;

import java.util.ArrayList;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.employee.model.vo.Department;
import com.kh.flokrGroupware.employee.model.vo.Employee;

@Repository
public class OrganizationDao {

    @Autowired
    private SqlSessionTemplate sqlSession;
    
    /**
     * 부서 목록 조회
     * @return 부서 목록
     */
    public ArrayList<Department> selectDepartmentList() {
        return (ArrayList)sqlSession.selectList("organizationMapper.selectDepartmentList");
    }
    
    /**
     * 부서 정보 조회
     * @param deptNo 부서 번호
     * @return 부서 정보
     */
    public Department selectDepartment(int deptNo) {
        return sqlSession.selectOne("organizationMapper.selectDepartment", deptNo);
    }
    
    /**
     * 부서 등록
     * @param dept 등록할 부서 정보
     * @return 처리 결과 (1: 성공, 0: 실패)
     */
    public int insertDepartment(Department dept) {
        return sqlSession.insert("organizationMapper.insertDepartment", dept);
    }
    
    /**
     * 부서 정보 수정
     * @param dept 수정할 부서 정보
     * @return 처리 결과 (1: 성공, 0: 실패)
     */
    public int updateDepartment(Department dept) {
        return sqlSession.update("organizationMapper.updateDepartment", dept);
    }
    
    /**
     * 부서 삭제 (상태 변경)
     * @param deptNo 삭제할 부서 번호
     * @return 처리 결과 (1: 성공, 0: 실패)
     */
    public int deleteDepartment(int deptNo) {
        return sqlSession.update("organizationMapper.deleteDepartment", deptNo);
    }
    
    /**
     * 부서별 직원 목록 조회
     * @param deptNo 부서 번호
     * @return 해당 부서 직원 목록
     */
    public ArrayList<Employee> selectEmployeesByDept(int deptNo) {
        return (ArrayList)sqlSession.selectList("organizationMapper.selectEmployeesByDept", deptNo);
    }
    
    /**
     * 직원 상세 정보 조회
     * @param empNo 직원 번호
     * @return 직원 정보
     */
    public Employee selectEmployee(int empNo) {
        // employeeMapper에 있는 기존 메소드 활용
        return sqlSession.selectOne("employeeMapper.selectEmployee", empNo);
    }
}