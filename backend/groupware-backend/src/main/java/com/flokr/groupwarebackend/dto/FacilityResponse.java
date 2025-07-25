package com.flokr.groupwarebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityResponse {
    private Long facilityNo;
    private String facilityName;
    private String facilityLocation;
    private Integer capacity;
    private String description;
    private String imagePath;
}