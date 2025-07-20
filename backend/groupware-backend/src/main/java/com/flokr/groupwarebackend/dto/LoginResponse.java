package com.flokr.groupwarebackend.dto;

import com.flokr.groupwarebackend.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // 토큰 만료 시간 (초)

    // 사용자 정보
    private String empId;
    private String empName;
    private String email;
    private String departmentName;
    private String positionName;
    private String role;
    private String profileImageUrl;

    // Employee 엔티티로부터 LoginResponse 생성하는 편의 메소드
    public static LoginResponse from(Employee employee, String accessToken, Long expiresIn) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .empId(employee.getEmpId())
                .empName(employee.getEmpName())
                .email(employee.getEmail())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getDeptName() : null)
                .positionName(employee.getPosition() != null ? employee.getPosition().getPositionName() : null)
                .role(employee.getRoleEnum().name())  // 이 부분 수정!
                .profileImageUrl(employee.getProfileImgPath())  // 이 부분도 수정!
                .build();
    }
}