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

    // 예약자 정보 (알림 발송을 위해 필요)
    private Long reserverEmpNo;
    private String reserverName;
    private Long empNo; // 알림 발송용 empNo 필드 추가

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private ReservationStatus status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    // empNo getter/setter (알림에서 접근용)
    public Long getEmpNo() {
        // reserverEmpNo와 동일하게 처리
        return this.reserverEmpNo;
    }

    public void setEmpNo(Long empNo) {
        this.empNo = empNo;
        this.reserverEmpNo = empNo; // 일관성 유지
    }
}