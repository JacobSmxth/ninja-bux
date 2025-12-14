package dev.jsmitty.bux.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CodeNinjasLoginRequest(
    @NotBlank String user, @NotNull Double latitude, @NotNull Double longitude) {}
