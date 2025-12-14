package dev.jsmitty.bux.system.external.dto;

public record CodeNinjasLoginResult(
    String token,
    String studentId,
    String facilityId,
    String firstName,
    String lastName,
    String facilityName,
    Long tokenExpiresAt) {}
