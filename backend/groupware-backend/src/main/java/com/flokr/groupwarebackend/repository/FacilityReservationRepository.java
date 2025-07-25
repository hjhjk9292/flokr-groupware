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

    // 모든 메서드를 @Query로 명시적으로 작성
    @Query("SELECT r FROM FacilityReservation r WHERE r.reserver.empNo = :empNo ORDER BY r.createDate DESC")
    List<FacilityReservation> findByReserverEmpNoOrderByCreateDateDesc(@Param("empNo") Long empNo);

    @Query("SELECT r FROM FacilityReservation r WHERE r.facility.facilityNo = :facilityNo ORDER BY r.startTime ASC")
    List<FacilityReservation> findByFacilityFacilityNoOrderByStartTimeAsc(@Param("facilityNo") Long facilityNo);

    @Query("SELECT r FROM FacilityReservation r WHERE r.status = :status ORDER BY r.createDate DESC")
    List<FacilityReservation> findByStatusOrderByCreateDateDesc(@Param("status") ReservationStatus status);

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

    @Query("SELECT r FROM FacilityReservation r ORDER BY r.createDate DESC")
    List<FacilityReservation> findRecentReservations();
}