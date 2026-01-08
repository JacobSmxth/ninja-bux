package dev.jsmitty.bux.system.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating a purchase.
 *
 * <p>Used by {@link dev.jsmitty.bux.system.controller.PurchaseController}.
 */
public record PurchaseRequest(@NotNull(message = "Shop item ID is required") Long shopItemId) {}
