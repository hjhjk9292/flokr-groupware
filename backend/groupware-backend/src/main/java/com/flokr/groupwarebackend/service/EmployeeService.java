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

    // 사번 자동 생성을 위한 락 객체 (동시성 처리)
    private static final Object empIdGenerationLock = new Object();

    /**
     * 모든 활성 직원 목록 조회
     * @return 활성 상태인 직원 목록
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByStatus("Y");
    }

    /**
     * 직원 번호(PK)로 직원 조회
     * @param empNo 직원 번호
     * @return 해당 직원 엔티티 (존재하지 않으면 Optional.empty)
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmpNo(Long empNo) {
        return employeeRepository.findById(empNo);
    }

    /**
     * 직원 ID(로그인 ID)로 직원 조회
     * @param empId 직원 ID
     * @return 해당 직원 엔티티 (존재하지 않으면 Optional.empty)
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmpId(String empId) {
        return employeeRepository.findByEmpId(empId);
    }

    /**
     * 이메일로 직원 조회
     * @param email 이메일 주소
     * @return 해당 직원 엔티티 (존재하지 않으면 Optional.empty)
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    /**
     * 새로운 직원 생성
     * @param request 직원 생성 요청 DTO
     * @return 생성된 직원 엔티티
     */
    @Transactional
    public Employee createEmployee(EmployeeRequest request) {
        // 부서와 직급 엔티티 유효성 검증 및 조회
        Department department = departmentRepository.findById(request.getDeptNo())
                .orElseThrow(() -> new ValidationException("유효하지 않은 부서 번호: " + request.getDeptNo()));
        Position position = positionRepository.findById(request.getPositionNo())
                .orElseThrow(() -> new ValidationException("유효하지 않은 직급 번호: " + request.getPositionNo()));

        // EMP_ID 및 EMAIL 중복 확인 (이제 empId는 자동 생성되므로 email만 필수 체크)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) { // 이메일 필수가 아니므로 null 체크
            employeeRepository.findByEmail(request.getEmail()).ifPresent(e -> {
                throw new ValidationException("이미 사용 중인 이메일 주소: " + request.getEmail());
            });
        }

        Employee newEmployee = new Employee();
        newEmployee.setEmpName(request.getEmpName());

        // 사번 자동 생성 (레거시 로직 참고, 동시성 고려)
        newEmployee.setEmpId(generateNewEmpId(request.getDeptNo()));

        // 비밀번호 설정 (요청에 평문 비밀번호가 있다면 사용, 없으면 자동 생성)
        String initialPassword;
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            initialPassword = request.getPassword(); // 관리자가 직접 설정한 경우
        } else {
            // 레거시와 동일하게 사번 + "init"으로 초기 비밀번호 생성
            initialPassword = newEmployee.getEmpId() + "init";
        }
        newEmployee.setPasswordHash(passwordEncoder.encode(initialPassword)); // 비밀번호 암호화

        // 이메일 설정 (요청에 이메일이 없다면 자동 생성)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            newEmployee.setEmail(request.getEmail());
        } else {
            newEmployee.setEmail(newEmployee.getEmpId() + "@flokr.com"); // 레거시와 동일하게 자동 생성
        }

        newEmployee.setPhone(request.getPhone());
        newEmployee.setDepartment(department); // 조회된 부서 엔티티 설정
        newEmployee.setPosition(position);     // 조회된 직급 엔티티 설정
        newEmployee.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDateTime.now()); // 입사일 설정 또는 현재 시각
        newEmployee.setProfileImageUrl(request.getProfileImageUrl());
        newEmployee.setSignatureImageUrl(request.getSignatureImageUrl());
        newEmployee.setIsAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : "N"); // 기본값 'N'
        newEmployee.setStatus(request.getStatus() != null ? request.getStatus() : "Y"); // 기본값 'Y'

        newEmployee.setCreateDate(LocalDateTime.now());
        // updateDate는 @PreUpdate로 자동 설정

        return employeeRepository.save(newEmployee);
    }

    // 사번 자동 생성 로직 (레거시 참고, 동시성 안전)
    private String generateNewEmpId(Long deptNo) {
        synchronized (empIdGenerationLock) { // 동시성 문제 방지를 위한 락
            String currentYearTwoDigit = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy"));
            String deptNoStr = String.valueOf(deptNo);

            Optional<String> lastEmpIdOpt = employeeRepository.findTopByEmpIdStartingWithDeptNoAndYearPrefix(deptNoStr, currentYearTwoDigit);

            int newSequence = 1;
            if (lastEmpIdOpt.isPresent()) {
                String lastEmpId = lastEmpIdOpt.get();
                // 마지막 3자리(순번) 추출 후 1 증가
                newSequence = Integer.parseInt(lastEmpId.substring(lastEmpId.length() - 3)) + 1;
            }

            return deptNoStr + currentYearTwoDigit + String.format("%03d", newSequence);
        }
    }

    /**
     * 기존 직원 정보 수정
     * @param empNo 수정할 직원 번호(PK)
     * @param request 직원 수정 요청 DTO
     * @return 수정된 직원 엔티티 (직원이 존재하지 않으면 Optional.empty)
     */
    @Transactional
    public Optional<Employee> updateEmployee(Long empNo, EmployeeRequest request) {
        return employeeRepository.findById(empNo).map(employee -> {
            // 부서와 직급 엔티티 유효성 검증 및 조회
            Department department = departmentRepository.findById(request.getDeptNo())
                    .orElseThrow(() -> new ValidationException("유효하지 않은 부서 번호: " + request.getDeptNo()));
            Position position = positionRepository.findById(request.getPositionNo())
                    .orElseThrow(() -> new ValidationException("유효하지 않은 직급 번호: " + request.getPositionNo()));

            // 이메일 중복 확인 (단, 본인 이메일 제외)
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                employeeRepository.findByEmail(request.getEmail()).ifPresent(e -> {
                    if (!e.getEmpNo().equals(empNo)) { // 본인이 아닌 다른 직원이 이미 해당 이메일을 사용하는 경우
                        throw new ValidationException("이미 사용 중인 이메일 주소: " + request.getEmail());
                    }
                });
            }

            employee.setEmpName(request.getEmpName());
            // 비밀번호는 변경 요청이 있을 때만 수정 (평문 -> 해시)
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            }
            employee.setEmail(request.getEmail());
            employee.setPhone(request.getPhone());
            employee.setDepartment(department);
            employee.setPosition(position);
            employee.setHireDate(request.getHireDate() != null ? request.getHireDate() : employee.getHireDate()); // 기존 값이 없거나 요청에 있다면 설정
            employee.setProfileImageUrl(request.getProfileImageUrl());
            employee.setSignatureImageUrl(request.getSignatureImageUrl());
            employee.setIsAdmin(request.getIsAdmin() != null ? request.getIsAdmin() : employee.getIsAdmin());
            employee.setStatus(request.getStatus() != null ? request.getStatus() : employee.getStatus());

            return employeeRepository.save(employee);
        });
    }

    /**
     * 직원 삭제(비활성화) (실제 데이터는 유지하고 상태만 'N'으로 변경)
     * @param empNo 삭제할 직원 번호(PK)
     * @return 삭제 성공 여부 (true: 성공, false: 직원 없음)
     */
    @Transactional
    public boolean deleteEmployee(Long empNo) {
        return employeeRepository.findById(empNo).map(employee -> {
            employee.setStatus("N"); // 상태를 'N'으로 변경
            employeeRepository.save(employee); // 변경된 상태 저장
            return true;
        }).orElse(false);
    }

    /**
     * 이름으로 직원 검색
     * @param name 검색할 직원의 이름
     * @return 검색된 직원 목록
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(String name) {
        return employeeRepository.findByEmpNameContaining(name);
    }

    /**
     * 부서 및 직급으로 직원 조회
     * @param deptNo 부서 번호
     * @param positionNo 직급 번호
     * @return 해당 조건의 직원 목록
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartmentAndPosition(Long deptNo, Long positionNo) {
        if (positionNo == null) {
            // 직급 조건이 없으면 해당 부서의 모든 직원 반환
            return employeeRepository.findByDepartment_DeptNo(deptNo);
        } else {
            // 기존 로직 유지
            return employeeRepository.findByDepartmentAndPosition(deptNo, positionNo);
        }
    }

    /**
     * 전체 직원 수 조회 (대시보드 통계용)
     * @return 총 직원 수
     */
    @Transactional(readOnly = true)
    public long getTotalEmployeeCount() {
        return employeeRepository.count();
    }

    /**
     * 활성 상태인 직원 수 조회 (대시보드 통계용)
     * @return 활성 직원 수
     */
    @Transactional(readOnly = true)
    public long getActiveEmployeeCount() {
        return employeeRepository.countByStatus("Y");
    }

    /**
     * 특정 부서의 직원 수 조회 (대시보드 통계용)
     * @param deptNo 부서 번호
     * @return 해당 부서의 직원 수
     */
    @Transactional(readOnly = true)
    public long getEmployeeCountByDepartment(Long deptNo) {
        return departmentRepository.findById(deptNo) // 부서가 실제로 존재하는지 확인
                .map(d -> employeeRepository.findByDepartment_DeptNo(deptNo).size())
                .orElse(0); // 부서가 없으면 0 반환
    }

    /**
     * 특정 월에 입사한 신규 직원 수 조회 (대시보드 통계용)
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 해당 월의 신규 입사자 수
     */
    @Transactional(readOnly = true)
    public long getNewHiresCountMonthly(int year, int month) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
        return employeeRepository.countByHireDateBetween(startOfMonth, endOfMonth); // Repository에 추가 필요
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