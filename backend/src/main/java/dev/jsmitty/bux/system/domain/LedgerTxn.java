package dev.jsmitty.bux.system.domain;

import jakarta.persistence.*;
import jakarta.persistence.Index;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the {@code ledger_txns} table.
 *
 * <p>Represents the source-of-truth balance history. Positive amounts award bux, negative
 * amounts spend bux. Created by {@link dev.jsmitty.bux.system.service.LedgerService} for
 * sync rewards, purchases, and adjustments.
 */
@Entity
@Table(
        name = "ledger_txns",
        indexes = {
            @Index(
                    name = "idx_ledger_facility_student_created",
                    columnList = "facility_id, student_id, created_at DESC")
        })
public class LedgerTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    /** Signed amount; positive earns, negative spends. */
    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TxnType type;

    private String description;

    /** Optional link to a purchase or adjustment id (not enforced by FK). */
    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public LedgerTxn() {}

    public LedgerTxn(
            UUID facilityId, String studentId, Integer amount, TxnType type, String description) {
        this.facilityId = facilityId;
        this.studentId = studentId;
        this.amount = amount;
        this.type = type;
        this.description = description;
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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public TxnType getType() {
        return type;
    }

    public void setType(TxnType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
