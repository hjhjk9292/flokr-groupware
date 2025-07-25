package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "FACILITY_RESERVATION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacilityReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESERVATION_NO")
    private Long reservationNo;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "reservations"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FACILITY_NO", nullable = false)
    private Facility facility;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "department", "position"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESERVER_EMP_NO", nullable = false)
    private Employee reserver;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "PURPOSE", length = 255)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "RES_STATUS", length = 10)
    private ReservationStatus status = ReservationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    // 생성자 (Builder 대신 사용)
    public FacilityReservation(Facility facility, Employee reserver, LocalDateTime startTime,
                               LocalDateTime endTime, String purpose) {
        this.facility = facility;
        this.reserver = reserver;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = ReservationStatus.PENDING;
    }

    // Helper methods
    public String getFacilityName() {
        return facility != null ? facility.getFacilityName() : null;
    }

    public String getReserverName() {
        return reserver != null ? reserver.getEmpName() : null;
    }

    public Long getFacilityNo() {
        return facility != null ? facility.getFacilityNo() : null;
    }

    public Long getReserverEmpNo() {
        return reserver != null ? reserver.getEmpNo() : null;
    }
}