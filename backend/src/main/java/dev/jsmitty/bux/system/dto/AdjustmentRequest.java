package dev.jsmitty.bux.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating a manual adjustment.
 */
public record AdjustmentRequest(
        @NotNull(message = "Amount is required") Integer amount,
        @NotBlank(message = "Reason is required") String reason) {}
