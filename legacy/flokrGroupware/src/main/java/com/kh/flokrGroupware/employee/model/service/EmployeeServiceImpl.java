package com.kh.flokrGroupware.employee.model.service;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.common.model.vo.PageInfo;
import com.kh.flokrGroupware.employee.model.dao.EmployeeDao;
import com.kh.flokrGroupware.employee.model.vo.Department;
import com.kh.flokrGroupware.employee.model.vo.Employee;
import com.kh.flokrGroupware.employee.model.vo.Position;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeDao employeeDao;
    
    @Override
    public Employee loginEmployee(Employee e) {
        return employeeDao.loginEmployee(e);
    }
    
    @Override
    public ArrayList<Department> selectDepartmentList() {
        return employeeDao.selectDepartmentList();
    }
    
    @Override
    public ArrayList<Position> selectPositionList() {
        return employeeDao.selectPositionList();
    }
    
    @Override
    public String getLastEmployeeId(int deptNo, String yearPrefix) {
        return employeeDao.getLastEmployeeId(deptNo, yearPrefix);
    }
    
    @Override
    public int insertEmployee(Employee e) {
        return employeeDao.insertEmployee(e);
    }
    
    @Override
    public int getEmployeeCount(Employee searchCondition) {
        return employeeDao.getEmployeeCount(searchCondition);
    }
    
    @Override
    public ArrayList<Employee> selectEmployeeList(Map<String, Object> params) {
        return employeeDao.selectEmployeeList(params);
    }
    
    @Override
    public Employee selectEmployee(int empNo) {
        return employeeDao.selectEmployee(empNo);
    }
    
    @Override
    public int updateEmployee(Employee e) {
        return employeeDao.updateEmployee(e);
    }
    
    @Override
    public int deleteEmployee(int empNo) {
        return employeeDao.deleteEmployee(empNo);
    }
    
    @Override
    public int resetPassword(Map<String, Object> params) {
        return employeeDao.resetPassword(params);
    }
    
    @Override
    public ArrayList<Map<String, Object>> searchEmployee(String keyword) {
        return employeeDao.searchEmployee(keyword);
    }

	@Override
	public ArrayList<Employee> findActiveEmployeesWithDeptAndPosition() {
		return employeeDao.selectActiveEmployeesWithDeptAndPosition();
	}
}