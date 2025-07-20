package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String empId) throws UsernameNotFoundException {
        log.debug("Authenticating user: {}", empId);
        
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with id: " + empId));
        
        // 비활성 사용자 체크
        if (employee.getStatus() != Employee.EmployeeStatus.ACTIVE) {
            throw new UsernameNotFoundException("Employee is not active: " + empId);
        }
        
        return createUserDetails(employee);
    }
    
    // Employee로 UserDetails 생성
    private UserDetails createUserDetails(Employee employee) {
        Collection<GrantedAuthority> authorities = createAuthorities(employee);
        
        return User.builder()
                .username(employee.getEmpId())
                .password(employee.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(employee.getStatus() != Employee.EmployeeStatus.ACTIVE)
                .credentialsExpired(false)
                .disabled(employee.getStatus() == Employee.EmployeeStatus.TERMINATED)
                .build();
    }
    
    // 권한 생성
    private Collection<GrantedAuthority> createAuthorities(Employee employee) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 기본 권한
        authorities.add(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
        
        // 부서 관리자 권한 (부서장인 경우)
        if (employee.getDepartment() != null && 
            employee.getDepartment().getManager() != null &&
            employee.getEmpId().equals(employee.getDepartment().getManager().getEmpId())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_DEPT_MANAGER"));
        }
        
        log.debug("User {} authorities: {}", employee.getEmpId(), authorities);
        return authorities;
    }
    
    // Employee 엔티티 조회 (JWT에서 사용)
    @Transactional(readOnly = true)
    public Employee findByEmpId(String empId) {
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with id: " + empId));
    }
    
    // 이메일로 Employee 조회 (로그인에서 사용 가능)
    @Transactional(readOnly = true)
    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with email: " + email));
    }
}
