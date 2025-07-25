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

    // 비밀번호 필드는 선택사항으로 변경 (@NotBlank 제거)
    private String password;

    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    private String phone;

    @NotNull(message = "부서 번호는 필수입니다.")
    private Long deptNo;

    @NotNull(message = "직급 번호는 필수입니다.")
    private Long positionNo;

    private LocalDateTime hireDate;

    private String profileImageUrl;

    private String signatureImageUrl;

    private String isAdmin;

    private String status;
}