package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "FACILITY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FACILITY_NO")
    private Long facilityNo;

    @Column(name = "FACILITY_NAME", nullable = false, length = 100)
    private String facilityName;

    @Column(name = "FACILITY_LOCATION", length = 255)
    private String facilityLocation;

    @Column(name = "CAPACITY")
    private Integer capacity;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "IMAGE_PATH", length = 255)
    private String imagePath;

    // OneToMany relationship with reservations
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "facility"})
    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FacilityReservation> reservations = new ArrayList<>();
}