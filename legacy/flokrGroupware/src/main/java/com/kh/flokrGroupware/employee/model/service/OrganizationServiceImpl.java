package com.kh.flokrGroupware.employee.model.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.flokrGroupware.employee.model.dao.OrganizationDao;
import com.kh.flokrGroupware.employee.model.vo.Department;
import com.kh.flokrGroupware.employee.model.vo.Employee;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationDao organizationDao;
    
    @Override
    public ArrayList<Department> selectDepartmentList() {
        return organizationDao.selectDepartmentList();
    }
    
    @Override
    public Department selectDepartment(int deptNo) {
        return organizationDao.selectDepartment(deptNo);
    }
    
    @Override
    public int insertDepartment(Department dept) {
        return organizationDao.insertDepartment(dept);
    }
    
    @Override
    public int updateDepartment(Department dept) {
        return organizationDao.updateDepartment(dept);
    }
    
    @Override
    public int deleteDepartment(int deptNo) {
        return organizationDao.deleteDepartment(deptNo);
    }
    
    @Override
    public ArrayList<Employee> selectEmployeesByDept(int deptNo) {
        return organizationDao.selectEmployeesByDept(deptNo);
    }
    
    @Override
    public Employee selectEmployee(int empNo) {
        return organizationDao.selectEmployee(empNo);
    }
}