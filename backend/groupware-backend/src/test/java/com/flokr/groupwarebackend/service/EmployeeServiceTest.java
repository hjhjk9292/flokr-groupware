package com.flokr.groupwarebackend.service;

import com.flokr.groupwarebackend.dto.EmployeeRequest;
import com.flokr.groupwarebackend.entity.Department;
import com.flokr.groupwarebackend.entity.Employee;
import com.flokr.groupwarebackend.entity.Position;
import com.flokr.groupwarebackend.repository.DepartmentRepository;
import com.flokr.groupwarebackend.repository.EmployeeRepository;
import com.flokr.groupwarebackend.repository.PositionRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * EmployeeService 테스트
 * 
 * 직원 관리 서비스의 주요 기능들을 테스트합니다.
 * Mock 객체를 사용하여 단위 테스트를 수행합니다.
 * 
 * @author hjhjk9292
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService 테스트")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private DepartmentRepository departmentRepository;
    
    @Mock
    private PositionRepository positionRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private EmployeeService employeeService;
    
    private Department testDepartment;
    private Position testPosition;
    private Employee testEmployee;
    private EmployeeRequest testRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 부서 설정
        testDepartment = new Department();
        testDepartment.setDeptNo(1L);
        testDepartment.setDeptName("개발팀");
        
        // 테스트용 직급 설정
        testPosition = new Position();
        testPosition.setPositionNo(1L);
        testPosition.setPositionName("사원");
        
        // 테스트용 직원 설정
        testEmployee = new Employee();
        testEmployee.setEmpNo(1L);
        testEmployee.setEmpId("125001");
        testEmployee.setEmpName("김철수");
        testEmployee.setEmail("kim@flokr.com");
        testEmployee.setPhone("010-1234-5678");
        testEmployee.setDepartment(testDepartment);
        testEmployee.setPosition(testPosition);
        testEmployee.setStatus("Y");
        testEmployee.setIsAdmin("N");
        testEmployee.setHireDate(LocalDateTime.now());
        testEmployee.setCreateDate(LocalDateTime.now());
        
        // 테스트용 요청 DTO 설정
        testRequest = new EmployeeRequest();
        testRequest.setEmpName("김철수");
        testRequest.setEmail("kim@flokr.com");
        testRequest.setPhone("010-1234-5678");
        testRequest.setDeptNo(1L);
        testRequest.setPositionNo(1L);
        testRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("직원 조회 테스트")
    class EmployeeRetrievalTest {

        @Test
        @DisplayName("활성 직원 목록 조회 성공")
        void getAllActiveEmployees_Success() {
            // Given
            List<Employee> activeEmployees = Arrays.asList(testEmployee);
            given(employeeRepository.findByStatus("Y")).willReturn(activeEmployees);

            // When
            List<Employee> result = employeeService.getAllActiveEmployees();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmpName()).isEqualTo("김철수");
            assertThat(result.get(0).getStatus()).isEqualTo("Y");
        }

        @Test
        @DisplayName("직원 번호로 조회 성공")
        void getEmployeeByEmpNo_Success() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(testEmployee));

            // When
            Optional<Employee> result = employeeService.getEmployeeByEmpNo(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmpName()).isEqualTo("김철수");
        }

        @Test
        @DisplayName("존재하지 않는 직원 번호로 조회 시 빈 결과")
        void getEmployeeByEmpNo_NotFound() {
            // Given
            given(employeeRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<Employee> result = employeeService.getEmployeeByEmpNo(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("직원 ID로 조회 성공")
        void getEmployeeByEmpId_Success() {
            // Given
            given(employeeRepository.findByEmpId("125001")).willReturn(Optional.of(testEmployee));

            // When
            Optional<Employee> result = employeeService.getEmployeeByEmpId("125001");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmpId()).isEqualTo("125001");
        }

        @Test
        @DisplayName("유효하지 않은 직원 ID로 조회 시 빈 결과")
        void getEmployeeByEmpId_InvalidInput() {
            // When & Then
            assertThat(employeeService.getEmployeeByEmpId(null)).isEmpty();
            assertThat(employeeService.getEmployeeByEmpId("")).isEmpty();
            assertThat(employeeService.getEmployeeByEmpId("   ")).isEmpty();
        }

        @Test
        @DisplayName("이메일로 조회 성공")
        void getEmployeeByEmail_Success() {
            // Given
            given(employeeRepository.findByEmail("kim@flokr.com")).willReturn(Optional.of(testEmployee));

            // When
            Optional<Employee> result = employeeService.getEmployeeByEmail("kim@flokr.com");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("kim@flokr.com");
        }
    }

    @Nested
    @DisplayName("직원 생성 테스트")
    class EmployeeCreationTest {

        @Test
        @DisplayName("새로운 직원 생성 성공")
        void createEmployee_Success() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(testDepartment));
            given(positionRepository.findById(1L)).willReturn(Optional.of(testPosition));
            given(employeeRepository.findByEmail("kim@flokr.com")).willReturn(Optional.empty());
            given(employeeRepository.findTopByEmpIdStartingWithDeptNoAndYearPrefix(anyString(), anyString()))
                    .willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(employeeRepository.save(any(Employee.class))).willReturn(testEmployee);

            // When
            Employee result = employeeService.createEmployee(testRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmpName()).isEqualTo("김철수");
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("유효하지 않은 부서로 직원 생성 시 예외 발생")
        void createEmployee_InvalidDepartment() {
            // Given
            given(departmentRepository.findById(999L)).willReturn(Optional.empty());
            testRequest.setDeptNo(999L);

            // When & Then
            assertThatThrownBy(() -> employeeService.createEmployee(testRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("유효하지 않은 부서 번호");
        }

        @Test
        @DisplayName("유효하지 않은 직급으로 직원 생성 시 예외 발생")
        void createEmployee_InvalidPosition() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(testDepartment));
            given(positionRepository.findById(999L)).willReturn(Optional.empty());
            testRequest.setPositionNo(999L);

            // When & Then
            assertThatThrownBy(() -> employeeService.createEmployee(testRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("유효하지 않은 직급 번호");
        }

        @Test
        @DisplayName("중복된 이메일로 직원 생성 시 예외 발생")
        void createEmployee_DuplicateEmail() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(testDepartment));
            given(positionRepository.findById(1L)).willReturn(Optional.of(testPosition));
            given(employeeRepository.findByEmail("kim@flokr.com")).willReturn(Optional.of(testEmployee));

            // When & Then
            assertThatThrownBy(() -> employeeService.createEmployee(testRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("이미 사용 중인 이메일 주소");
        }

        @Test
        @DisplayName("이메일 없이 직원 생성 시 자동 이메일 생성")
        void createEmployee_AutoGenerateEmail() {
            // Given
            testRequest.setEmail(null);
            given(departmentRepository.findById(1L)).willReturn(Optional.of(testDepartment));
            given(positionRepository.findById(1L)).willReturn(Optional.of(testPosition));
            given(employeeRepository.findTopByEmpIdStartingWithDeptNoAndYearPrefix(anyString(), anyString()))
                    .willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(employeeRepository.save(any(Employee.class))).willAnswer(invocation -> {
                Employee savedEmployee = invocation.getArgument(0);
                assertThat(savedEmployee.getEmail()).endsWith("@flokr.com");
                return savedEmployee;
            });

            // When
            employeeService.createEmployee(testRequest);

            // Then
            verify(employeeRepository).save(any(Employee.class));
        }
    }

    @Nested
    @DisplayName("직원 수정 테스트")
    class EmployeeUpdateTest {

        @Test
        @DisplayName("직원 정보 수정 성공")
        void updateEmployee_Success() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(testEmployee));
            given(departmentRepository.findById(1L)).willReturn(Optional.of(testDepartment));
            given(positionRepository.findById(1L)).willReturn(Optional.of(testPosition));
            given(employeeRepository.findByEmail("kim@flokr.com")).willReturn(Optional.of(testEmployee));
            given(employeeRepository.save(any(Employee.class))).willReturn(testEmployee);

            // When
            Optional<Employee> result = employeeService.updateEmployee(1L, testRequest);

            // Then
            assertThat(result).isPresent();
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("존재하지 않는 직원 수정 시 빈 결과")
        void updateEmployee_NotFound() {
            // Given
            given(employeeRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<Employee> result = employeeService.updateEmployee(999L, testRequest);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 직원이 사용 중인 이메일로 수정 시 예외 발생")
        void updateEmployee_DuplicateEmail() {
            // Given
            Employee anotherEmployee = new Employee();
            anotherEmployee.setEmpNo(2L);
            anotherEmployee.setEmail("kim@flokr.com");

            given(employeeRepository.findById(1L)).willReturn(Optional.of(testEmployee));
            given(departmentRepository.findById(1L)).willReturn(Optional.of(testDepartment));
            given(positionRepository.findById(1L)).willReturn(Optional.of(testPosition));
            given(employeeRepository.findByEmail("kim@flokr.com")).willReturn(Optional.of(anotherEmployee));

            // When & Then
            assertThatThrownBy(() -> employeeService.updateEmployee(1L, testRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("이미 사용 중인 이메일 주소");
        }
    }

    @Nested
    @DisplayName("직원 삭제 테스트")
    class EmployeeDeletionTest {

        @Test
        @DisplayName("직원 삭제(비활성화) 성공")
        void deleteEmployee_Success() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(testEmployee));
            given(employeeRepository.save(any(Employee.class))).willReturn(testEmployee);

            // When
            boolean result = employeeService.deleteEmployee(1L);

            // Then
            assertThat(result).isTrue();
            verify(employeeRepository).save(any(Employee.class));
        }

        @Test
        @DisplayName("존재하지 않는 직원 삭제 시 실패")
        void deleteEmployee_NotFound() {
            // Given
            given(employeeRepository.findById(999L)).willReturn(Optional.empty());

            // When
            boolean result = employeeService.deleteEmployee(999L);

            // Then
            assertThat(result).isFalse();
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Nested
    @DisplayName("직원 검색 테스트")
    class EmployeeSearchTest {

        @Test
        @DisplayName("이름으로 직원 검색 성공")
        void searchEmployeesByName_Success() {
            // Given
            List<Employee> searchResult = Arrays.asList(testEmployee);
            given(employeeRepository.findByEmpNameContaining("김철수")).willReturn(searchResult);

            // When
            List<Employee> result = employeeService.searchEmployeesByName("김철수");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmpName()).isEqualTo("김철수");
        }

        @Test
        @DisplayName("빈 검색어로 검색 시 빈 목록 반환")
        void searchEmployeesByName_EmptyKeyword() {
            // When & Then
            assertThat(employeeService.searchEmployeesByName(null)).isEmpty();
            assertThat(employeeService.searchEmployeesByName("")).isEmpty();
            assertThat(employeeService.searchEmployeesByName("   ")).isEmpty();
        }

        @Test
        @DisplayName("부서별 직원 조회 성공")
        void getEmployeesByDepartmentAndPosition_Success() {
            // Given
            List<Employee> deptEmployees = Arrays.asList(testEmployee);
            given(employeeRepository.findByDepartment_DeptNo(1L)).willReturn(deptEmployees);

            // When
            List<Employee> result = employeeService.getEmployeesByDepartmentAndPosition(1L, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDepartment().getDeptNo()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("통계 조회 테스트")
    class StatisticsTest {

        @Test
        @DisplayName("전체 직원 수 조회")
        void getTotalEmployeeCount() {
            // Given
            given(employeeRepository.count()).willReturn(10L);

            // When
            long result = employeeService.getTotalEmployeeCount();

            // Then
            assertThat(result).isEqualTo(10L);
        }

        @Test
        @DisplayName("활성 직원 수 조회")
        void getActiveEmployeeCount() {
            // Given
            given(employeeRepository.countByStatus("Y")).willReturn(8L);

            // When
            long result = employeeService.getActiveEmployeeCount();

            // Then
            assertThat(result).isEqualTo(8L);
        }

        @Test
        @DisplayName("부서별 직원 수 조회")
        void getEmployeeCountByDepartment() {
            // Given
            List<Employee> deptEmployees = Arrays.asList(testEmployee);
            given(departmentRepository.findById(1L)).willReturn(Optional.of(testDepartment));
            given(employeeRepository.findByDepartment_DeptNo(1L)).willReturn(deptEmployees);

            // When
            long result = employeeService.getEmployeeCountByDepartment(1L);

            // Then
            assertThat(result).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 부서의 직원 수 조회 시 0 반환")
        void getEmployeeCountByDepartment_NotFound() {
            // Given
            given(departmentRepository.findById(999L)).willReturn(Optional.empty());

            // When
            long result = employeeService.getEmployeeCountByDepartment(999L);

            // Then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("월별 신규 입사자 수 조회")
        void getNewHiresCountMonthly() {
            // Given
            given(employeeRepository.countByHireDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(3L);

            // When
            long result = employeeService.getNewHiresCountMonthly(2025, 7);

            // Then
            assertThat(result).isEqualTo(3L);
        }
    }
}
