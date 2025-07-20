package com.kh.flokrGroupware.employee.model.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.employee.model.vo.Department;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.employee.model.vo.Position;

@Repository
public class EmployeeDao {

    @Autowired
    private SqlSessionTemplate sqlSession;
    
    public Employee loginEmployee(Employee e) {
        return sqlSession.selectOne("employeeMapper.loginEmployee", e);
    }
    
    public ArrayList<Department> selectDepartmentList() {
        return (ArrayList)sqlSession.selectList("employeeMapper.selectDepartmentList");
    }
    
    public ArrayList<Position> selectPositionList() {
        return (ArrayList)sqlSession.selectList("employeeMapper.selectPositionList");
    }
    
    public String getLastEmployeeId(int deptNo, String yearPrefix) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("deptNo", deptNo);
        paramMap.put("yearPrefix", yearPrefix);
        return sqlSession.selectOne("employeeMapper.getLastEmployeeId", paramMap);
    }
    
    public int insertEmployee(Employee e) {
        return sqlSession.insert("employeeMapper.insertEmployee", e);
    }
    
    public int getEmployeeCount(Employee searchCondition) {
        return sqlSession.selectOne("employeeMapper.getEmployeeCount", searchCondition);
    }
    
    public ArrayList<Employee> selectEmployeeList(Map<String, Object> params) {
        return (ArrayList)sqlSession.selectList("employeeMapper.selectEmployeeList", params);
    }
    
    public Employee selectEmployee(int empNo) {
        return sqlSession.selectOne("employeeMapper.selectEmployee", empNo);
    }
    
    public int updateEmployee(Employee e) {
        return sqlSession.update("employeeMapper.updateEmployee", e);
    }
    
    public int deleteEmployee(int empNo) {
        return sqlSession.update("employeeMapper.deleteEmployee", empNo);
    }
    
    public int resetPassword(Map<String, Object> params) {
        return sqlSession.update("employeeMapper.resetPassword", params);
    }
    
    public ArrayList<Map<String, Object>> searchEmployee(String keyword) {
        return (ArrayList)sqlSession.selectList("employeeMapper.searchEmployee", keyword);
    }
    
    public ArrayList<Employee> selectActiveEmployeesWithDeptAndPosition() {
    	return (ArrayList)sqlSession.selectList("employeeMapper.findAllActiveEmployeesWithDeptAndPosition");
    }
}