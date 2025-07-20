package com.flokr.groupwarebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "부서명은 필수입니다.")
    private String deptName;

    // 부서 상태를 생성/수정 시에도 받을 수 있도록 추가 (필요에 따라 NotBlank/Nullable 조정)
    private String status;
}