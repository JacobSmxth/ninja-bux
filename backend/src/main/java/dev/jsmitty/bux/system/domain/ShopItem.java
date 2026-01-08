package dev.jsmitty.bux.system.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the {@code shop_items} table.
 *
 * <p>Represents a purchasable item scoped to a facility. Managed by
 * {@link dev.jsmitty.bux.system.service.ShopService} and read during purchases.
 */
@Entity
@Table(name = "shop_items")
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @NotBlank(message = "Item name is required")
    @Column(nullable = false)
    private String name;

    private String description;

    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private Integer price;

    /** Flag for hiding/showing the item in the public shop list. */
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ShopItem() {}

    public ShopItem(UUID facilityId, String name, String description, Integer price) {
        this.facilityId = facilityId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isAvailable = true;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
