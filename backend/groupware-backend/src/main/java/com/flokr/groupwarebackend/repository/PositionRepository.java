package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    
    // 직급명으로 조회
    Optional<Position> findByPositionName(String positionName);
    
    // 직급 레벨별 조회
    List<Position> findByPositionLevelOrderByPositionLevelAsc();
    
    // 특정 레벨 이상 직급 조회
    List<Position> findByPositionLevelGreaterThanEqualOrderByPositionLevelAsc(Integer level);
    
    // 특정 레벨 이하 직급 조회
    List<Position> findByPositionLevelLessThanEqualOrderByPositionLevelAsc(Integer level);
    
    // 급여 범위별 직급 조회
    List<Position> findByBaseSalaryBetweenOrderByBaseSalaryAsc(Long minSalary, Long maxSalary);
    
    // 최고 레벨 직급 조회
    @Query("SELECT p FROM Position p WHERE p.positionLevel = (SELECT MAX(p2.positionLevel) FROM Position p2)")
    List<Position> findTopLevelPositions();
    
    // 최저 레벨 직급 조회
    @Query("SELECT p FROM Position p WHERE p.positionLevel = (SELECT MIN(p2.positionLevel) FROM Position p2)")
    List<Position> findEntryLevelPositions();
    
    // 직급별 직원 수 조회
    @Query("SELECT p.positionName, COUNT(e) FROM Position p LEFT JOIN p.employees e GROUP BY p.positionId, p.positionName ORDER BY p.positionLevel")
    List<Object[]> findPositionEmployeeCounts();
}
