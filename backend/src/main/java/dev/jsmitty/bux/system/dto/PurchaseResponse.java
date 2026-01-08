package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.domain.Purchase;
import dev.jsmitty.bux.system.domain.PurchaseStatus;

import java.time.LocalDateTime;

/**
 * Response payload for purchase creation and status updates.
 *
 * <p>Built by {@link dev.jsmitty.bux.system.service.PurchaseService}.
 */
public record PurchaseResponse(
        Long purchaseId,
        String itemName,
        Integer price,
        Integer newBalance,
        PurchaseStatus status,
        LocalDateTime fulfilledAt) {
    public static PurchaseResponse created(Purchase purchase, Integer newBalance) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getItemName(),
                purchase.getPrice(),
                newBalance,
                purchase.getStatus(),
                purchase.getFulfilledAt());
    }

    public static PurchaseResponse statusUpdate(
            Purchase purchase, boolean refunded, Integer newBalance) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getItemName(),
                purchase.getPrice(),
                newBalance,
                purchase.getStatus(),
                purchase.getFulfilledAt());
    }
}
