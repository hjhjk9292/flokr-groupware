package com.flokr.groupwarebackend.dto;

import com.flokr.groupwarebackend.dto.FacilityUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityUpdateRequest {
    private String facilityName;
    private String facilityLocation;
    private Integer capacity;
    private String description;
}