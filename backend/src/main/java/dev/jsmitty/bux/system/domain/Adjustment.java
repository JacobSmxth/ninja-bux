package dev.jsmitty.bux.system.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the {@code adjustments} table.
 *
 * <p>Represents a manual balance adjustment made by an admin. Each adjustment
 * is mirrored by a ledger transaction to keep balances consistent.
 */
@Entity
@Table(name = "adjustments")
public class Adjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    /** Admin id that performed the adjustment (not enforced by FK). */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private Integer amount;

    @NotBlank(message = "Reason is required")
    @Column(nullable = false)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Adjustment() {}

    public Adjustment(
            UUID facilityId, String studentId, Long adminId, Integer amount, String reason) {
        this.facilityId = facilityId;
        this.studentId = studentId;
        this.adminId = adminId;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(UUID facilityId) {
        this.facilityId = facilityId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
