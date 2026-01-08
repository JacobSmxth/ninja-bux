package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.domain.PurchaseStatus;

import java.time.LocalDateTime;

/**
 * Line item used by purchase list responses.
 *
 * <p>Includes display name derived from {@link dev.jsmitty.bux.system.domain.Ninja}.
 */
public record PurchaseListItem(
        Long id,
        String studentId,
        String ninjaName,
        String itemName,
        Integer price,
        PurchaseStatus status,
        LocalDateTime purchasedAt,
        LocalDateTime fulfilledAt) {}
