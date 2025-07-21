package com.flokr.groupwarebackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "직원명은 필수입니다.")
    private String empName;

    // empId는 자동 생성되므로 요청 DTO에서 제거 (생성 시에는 필요 없음)
    // private String empId;

    @NotBlank(message = "비밀번호는 필수입니다.") // 초기 생성 시 사용될 평문 비밀번호
    private String password;

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    // 이메일 자동 생성 로직이 있으므로 필수는 아님
    private String email;

    private String phone;

    @NotNull(message = "부서 번호는 필수입니다.")
    private Long deptNo; // 부서 번호 (Department 참조)

    @NotNull(message = "직급 번호는 필수입니다.")
    private Long positionNo; // 직급 번호 (Position 참조)

    private LocalDateTime hireDate; // 입사일 (없으면 현재 시점으로 자동 설정)

    private String profileImageUrl; // 프로필 이미지 경로

    private String signatureImageUrl; // 서명 이미지 경로

    private String isAdmin; // 관리자 여부 (Y/N) - 생성/수정 시 받을 수 있도록

    private String status; // 직원 상태 (Y/N) - 생성/수정 시 받을 수 있도록
}