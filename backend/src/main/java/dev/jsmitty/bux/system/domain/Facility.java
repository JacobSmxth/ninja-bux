package dev.jsmitty.bux.system.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the {@code facilities} table.
 *
 * <p>Facilities scope all student, shop, purchase, and adjustment data. They are linked to
 * admins via a join table and inferred during sync if missing.
 */
@Entity
@Table(name = "facilities")
public class Facility {

    @Id private UUID id;

    @NotBlank(message = "Facility name can't be blank")
    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Facility() {}

    public Facility(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
