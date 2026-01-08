package dev.jsmitty.bux.system.dto;

import java.util.List;

/**
 * Admin login response containing JWT and facility access list.
 */
public record LoginResponse(
        String token,
        Long adminId,
        String username,
        boolean superAdmin,
        List<FacilityResponse> facilities) {}
