package com.flokr.groupwarebackend.dto;

import com.flokr.groupwarebackend.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long reservationNo;
    private Long facilityNo;
    private String facilityName;
    private Long reserverEmpNo;
    private String reserverName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private ReservationStatus status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}