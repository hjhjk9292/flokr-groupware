package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "POSITION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POSITION_NO")
    private Long positionNo;

    @Column(name = "POSITION_NAME", nullable = false, length = 100)
    private String positionName;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "STATUS", length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String status = "Y";

    @JsonManagedReference // 추가: "직급"이 "직원" 목록을 관리 (직렬화 시 순환 시작 지점)
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees;
}