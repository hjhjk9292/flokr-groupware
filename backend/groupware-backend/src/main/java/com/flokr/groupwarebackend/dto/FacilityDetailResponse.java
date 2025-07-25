package com.flokr.groupwarebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDetailResponse {
    private FacilityResponse facility;
    private List<ReservationResponse> reservations;
}