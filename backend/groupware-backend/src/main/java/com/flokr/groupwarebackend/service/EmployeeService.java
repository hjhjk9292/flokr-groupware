package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.dto.EmployeeRequest;
import com.flokr.groupwarebackend.entity.Department;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.entity.Position;
import com.flokr.groupwarebackend.repository.DepartmentRepository;
import com.flokr.groupwarebackend.repository.EmployeeRepository;
import com.flokr.groupwarebackend.repository.PositionRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Object empIdGenerationLock = new Object();

    /**
     * 모든 활성 직원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByStatus("Y");
    }

    /**
     * 직원 번호로 직원 조회
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmpNo(Long empNo) {
        return employeeRepository.findById(empNo);
    }

    /**
     * 사번으로 직원 조회
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmpId(String empId) {
        return employeeRepository.findByEmpId(empId);
    }

    /**
     * 이메일로 직원 조회
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    /**
     * 새로운 직원 생성
     */
    @Transactional
    public Employee createEmployee(EmployeeRequest request) {
        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new ValidationException("유효하지 않은 부서 번호: " + request.getDeptNo()));
        Position position = positionRepository.findById(request.getPositionNo())
                .orElseThrow(() -> new ValidationException("유효하지 않은 직급 번호: " + request.getPositionNo()));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            employeeRepository.findByEmail(request.getEmail()).ifPresent(e -> {
                throw new ValidationException("이미 사용 중인 이메일 주소: " + request.getEmail());
            });
        }

        Employee newEmployee = new Employee();
        newEmployee.setEmpName(request.getEmpName());
        newEmployee.setEmpId(generateNewEmpId(request.getDeptNo()));

        String initialPassword = newEmployee.getEmpId() + "init";
        newEmployee.setPasswordHash(passwordEncoder.encode(initialPassword));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            newEmployee.setEmail(request.getEmail());
        } else {
            newEmployee.setEmail(newEmployee.getEmpId() + "@flokr.com");
        }

        newEmployee.setPhone(request.getPhone());
        newEmployee.setDepartment(department);
        newEmployee.setPosition(position);
        newEmployee.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDateTime.now());
        newEmployee.setProfileImageUrl(request.getProfileImageUrl());
        newEmployee.setSignatureImageUrl(request.getSignatureImageUrl());
        newEmployee.setIsAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : "N");
        newEmployee.setStatus(request.getStatus() != null ? request.getStatus() : "Y");
        newEmployee.setCreateDate(LocalDateTime.now());

        return employeeRepository.save(newEmployee);
    }

    private String generateNewEmpId(Long deptNo) {
        synchronized (empIdGenerationLock) {
            String currentYearTwoDigit = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy"));
            String deptNoStr = String.valueOf(deptNo);

            Optional<String> lastEmpIdOpt = employeeRepository.findTopByEmpIdStartingWithDeptNoAndYearPrefix(deptNoStr, currentYearTwoDigit);

            int newSequence = 1;
            if (lastEmpIdOpt.isPresent()) {
                String lastEmpId = lastEmpIdOpt.get();
                newSequence = Integer.parseInt(lastEmpId.substring(lastEmpId.length() - 3)) + 1;
            }

            return deptNoStr + currentYearTwoDigit + String.format("%03d", newSequence);
        }
    }

    /**
     * 기존 직원 정보 수정
     */
    @Transactional
    public Optional<Employee> updateEmployee(Long empNo, EmployeeRequest request) {
        return employeeRepository.findById(empNo).map(employee -> {
            Department department = departmentRepository.findById(request.getDeptNo())
                    .orElseThrow(() -> new ValidationException("유효하지 않은 부서 번호: " + request.getDeptNo()));
            Position position = positionRepository.findById(request.getPositionNo())
                    .orElseThrow(() -> new ValidationException("유효하지 않은 직급 번호: " + request.getPositionNo()));

            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                employeeRepository.findByEmail(request.getEmail()).ifPresent(e -> {
                    if (!e.getEmpNo().equals(empNo)) {
                        throw new ValidationException("이미 사용 중인 이메일 주소: " + request.getEmail());
                    }
                });
            }

            employee.setEmpName(request.getEmpName());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            employee.setEmail(request.getEmail());
            employee.setPhone(request.getPhone());
            employee.setDepartment(department);
            employee.setPosition(position);
            employee.setHireDate(request.getHireDate() != null ? request.getHireDate() : employee.getHireDate());
            employee.setProfileImageUrl(request.getProfileImageUrl());
            employee.setSignatureImageUrl(request.getSignatureImageUrl());
            employee.setIsAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : employee.getIsAdmin());
            employee.setStatus(request.getStatus() != null ? request.getStatus() : employee.getStatus());

            return employeeRepository.save(employee);
        });
    }

    /**
     * 비밀번호 초기화 (사번 + "init")
     */
    @Transactional
    public boolean resetPassword(Long empNo) {
        return employeeRepository.findById(empNo).map(employee -> {
            String initialPassword = employee.getEmpId() + "init";
            employee.setPasswordHash(passwordEncoder.encode(initialPassword));
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    /**
     * 직원 삭제 (상태를 'N'으로 변경)
     */
    @Transactional
    public boolean deleteEmployee(Long empNo) {
        return employeeRepository.findById(empNo).map(employee -> {
            employee.setStatus("N");
            employeeRepository.save(employee);
            return true;
        }).orElse(false);
    }

    /**
     * 이름으로 직원 검색
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(String name) {
        return employeeRepository.findByEmpNameContaining(name);
    }

    /**
     * 부서 및 직급으로 직원 조회
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartmentAndPosition(Long deptNo, Long positionNo) {
        if (positionNo == null) {
            return employeeRepository.findByDepartment_DeptNo(deptNo);
        } else {
            return employeeRepository.findByDepartmentAndPosition(deptNo, positionNo);
        }
    }

    /**
     * 전체 직원 수 조회 (대시보드용)
     */
    @Transactional(readOnly = true)
    public long getTotalEmployeeCount() {
        return employeeRepository.count();
    }

    /**
     * 활성 직원 수 조회 (대시보드용)
     */
    @Transactional(readOnly = true)
    public long getActiveEmployeeCount() {
        return employeeRepository.countByStatus("Y");
    }

    /**
     * 부서별 직원 수 조회 (대시보드용)
     */
    @Transactional(readOnly = true)
    public long getEmployeeCountByDepartment(Long deptNo) {
        return departmentRepository.findById(deptNo)
                .map(d -> employeeRepository.findByDepartment_DeptNo(deptNo).size())
                .orElse(0);
    }

    /**
     * 월별 신규 입사자 수 조회 (대시보드용)
     */
    @Transactional(readOnly = true)
    public long getNewHiresCountMonthly(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
        return employeeRepository.countByHireDateBetween(startOfMonth, endOfMonth);
    }

    /**
     * 부서별 직원 목록 조회 (알림용)
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long deptNo) {
        return employeeRepository.findByDepartment_DeptNo(deptNo);
    }

    /**
     * 직원 검색 (알림용)
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String keyword) {
        return employeeRepository.findByEmpNameContaining(keyword);
    }
}