package com.flokr.groupwarebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionRequest {

    @NotBlank(message = "직급명은 필수입니다.")
    private String positionName;

    // 직급 상태를 생성/수정 시에도 받을 수 있도록 추가
    private String status;
}