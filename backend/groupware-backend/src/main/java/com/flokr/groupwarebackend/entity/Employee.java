package com.flokr.groupwarebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "EMPLOYEE")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMP_NO")
    private Long empNo;

    @Column(name = "EMP_NAME", nullable = false, length = 100)
    private String empName;

    @Column(name = "EMP_ID", nullable = false, unique = true, length = 50)
    private String empId;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @JsonBackReference // "직원"이 "부서"를 참조 (이전 추가됨)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPT_NO", nullable = false, foreignKey = @ForeignKey(name = "FK_employee_department"))
    private Department department;

    @JsonBackReference // 추가: "직원"이 "직급"을 참조 (직렬화 시 순환 끊음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POSITION_NO", nullable = false, foreignKey = @ForeignKey(name = "FK_employee_position"))
    private Position position;

    @Column(name = "HIRE_DATE")
    private LocalDateTime hireDate;

    @Column(name = "PROFILE_IMG_PATH", length = 255)
    private String profileImageUrl;

    @Column(name = "SIGNATURE_IMG_PATH", length = 255)
    private String signatureImageUrl;

    @Column(name = "IS_ADMIN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String isAdmin = "N";

    @Column(name = "LAST_LOGIN_DATE")
    private LocalDateTime lastLoginDate;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column(name = "STATUS", length = 1, columnDefinition = "CHAR(1)")
    @Builder.Default
    private String status = "Y";

    @PreUpdate
    protected void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }

    // Helper methods for role conversion
    public EmployeeStatus getEmployeeStatus() {
        return "Y".equals(status) ? EmployeeStatus.ACTIVE : EmployeeStatus.INACTIVE;
    }

    public EmployeeRole getRole() {
        return "Y".equals(isAdmin) ? EmployeeRole.ADMIN : EmployeeRole.USER;
    }

    // 기존 메서드명과의 호환성을 위한 메서드들
    public EmployeeRole getRoleEnum() {
        return getRole();
    }

    public boolean isActive() {
        return "Y".equals(status);
    }

    public String getProfileImgPath() {
        return profileImageUrl;
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, TERMINATED
    }

    public enum EmployeeRole {
        USER, ADMIN, MANAGER
    }
}