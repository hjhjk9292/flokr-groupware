package com.flokr.groupwarebackend.repository;

import com.flokr.groupwarebackend.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findByFacilityNameContaining(String name);

    @Query("SELECT f FROM Facility f WHERE f.capacity >= :minCapacity")
    List<Facility> findByMinCapacity(@Param("minCapacity") Integer minCapacity);
}