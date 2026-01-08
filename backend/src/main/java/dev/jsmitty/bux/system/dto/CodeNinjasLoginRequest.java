package dev.jsmitty.bux.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for Code Ninjas login proxy/sync.
 */
public record CodeNinjasLoginRequest(
        @NotBlank String user, @NotNull Double latitude, @NotNull Double longitude) {}
