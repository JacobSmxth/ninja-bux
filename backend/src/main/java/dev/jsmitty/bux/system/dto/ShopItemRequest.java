package dev.jsmitty.bux.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for creating or updating shop items.
 */
public record ShopItemRequest(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Price is required") @Positive(message = "Price must be positive")
                Integer price,
        Boolean isAvailable) {}
