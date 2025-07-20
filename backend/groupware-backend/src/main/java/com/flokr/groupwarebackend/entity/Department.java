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
@Table(name = "DEPARTMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DEPT_NO")
    private Long deptNo;

    @Column(name = "DEPT_NAME", nullable = false, length = 100)
    private String deptName;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "STATUS", length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String status = "Y";

    @JsonManagedReference // "부서"가 "직원" 목록을 관리 (직렬화 시 순환 시작 지점)
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees;
}