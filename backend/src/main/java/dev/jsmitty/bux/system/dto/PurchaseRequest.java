package dev.jsmitty.bux.system.dto;

import jakarta.validation.constraints.NotNull;

public record PurchaseRequest(@NotNull(message = "Shop item ID is required") Long shopItemId) {}
