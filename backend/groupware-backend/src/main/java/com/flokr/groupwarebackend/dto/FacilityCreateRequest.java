package com.flokr.groupwarebackend.dto;

import com.flokr.groupwarebackend.dto.FacilityCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityCreateRequest {
    private String facilityName;
    private String facilityLocation;
    private Integer capacity;
    private String description;
}