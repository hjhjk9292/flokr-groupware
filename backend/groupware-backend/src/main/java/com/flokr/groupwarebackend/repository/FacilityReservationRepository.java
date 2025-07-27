package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.FacilityReservation;
import com.flokr.groupwarebackend.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacilityReservationRepository extends JpaRepository<FacilityReservation, Long> {

    // 기존 메서드들
    @Query("SELECT r FROM FacilityReservation r WHERE r.reserver.empNo = :empNo ORDER BY r.createDate DESC")
    List<FacilityReservation> findByReserverEmpNoOrderByCreateDateDesc(@Param("empNo") Long empNo);

    @Query("SELECT r FROM FacilityReservation r WHERE r.facility.facilityNo = :facilityNo ORDER BY r.startTime ASC")
    List<FacilityReservation> findByFacilityFacilityNoOrderByStartTimeAsc(@Param("facilityNo") Long facilityNo);

    @Query("SELECT r FROM FacilityReservation r WHERE r.status = :status ORDER BY r.createDate DESC")
    List<FacilityReservation> findByStatusOrderByCreateDateDesc(@Param("status") ReservationStatus status);

    @Query("SELECT r FROM FacilityReservation r ORDER BY r.createDate DESC")
    List<FacilityReservation> findRecentReservations();

    // 시간 충돌 예약 검사 (신규 예약용)
    @Query("SELECT r FROM FacilityReservation r WHERE r.facility.facilityNo = :facilityNo " +
            "AND r.status IN ('PENDING', 'APPROVED') " +
            "AND ((r.startTime <= :startTime AND r.endTime > :startTime) " +
            "OR (r.startTime < :endTime AND r.endTime >= :endTime) " +
            "OR (r.startTime >= :startTime AND r.endTime <= :endTime))")
    List<FacilityReservation> findConflictingReservations(
            @Param("facilityNo") Long facilityNo,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 시간 충돌 예약 검사 (특정 예약 제외) - 예약 수정용
     */
    @Query("SELECT r FROM FacilityReservation r WHERE r.facility.facilityNo = :facilityNo " +
            "AND r.reservationNo != :excludeReservationNo " +
            "AND r.status IN ('PENDING', 'APPROVED') " +
            "AND ((r.startTime <= :startTime AND r.endTime > :startTime) " +
            "OR (r.startTime < :endTime AND r.endTime >= :endTime) " +
            "OR (r.startTime >= :startTime AND r.endTime <= :endTime))")
    List<FacilityReservation> findConflictingReservationsExcluding(
            @Param("facilityNo") Long facilityNo,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeReservationNo") Long excludeReservationNo
    );
}