package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    // 직급명으로 조회
    Optional<Position> findByPositionName(String positionName);

    // 활성 상태 직급만 조회
    List<Position> findByStatusOrderByPositionNameAsc(String status);

    // 직급명으로 검색
    List<Position> findByPositionNameContaining(String keyword);

    // 직급별 직원 수 조회
    @Query("SELECT p.positionName, COUNT(e) FROM Position p LEFT JOIN p.employees e WHERE p.status = 'Y' GROUP BY p.positionNo, p.positionName ORDER BY p.positionName")
    List<Object[]> findPositionEmployeeCounts();

    // 모든 활성 직급 조회 (생성일순)
    List<Position> findByStatusOrderByCreateDateAsc(String status);
}