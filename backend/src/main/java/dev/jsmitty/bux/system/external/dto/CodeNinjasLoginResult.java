package dev.jsmitty.bux.system.external.dto;

/**
 * Normalized login data used by sync and facility resolution.
 *
 * <p>Built by {@link dev.jsmitty.bux.system.external.CodeNinjasApiClient}.
 */
public record CodeNinjasLoginResult(
        String token,
        String studentId,
        String facilityId,
        String firstName,
        String lastName,
        String facilityName,
        Long tokenExpiresAt) {}
