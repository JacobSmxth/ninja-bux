package dev.jsmitty.bux.system.dto;

import java.util.List;

public record LoginResponse(
        String token,
        Long adminId,
        String username,
        boolean superAdmin,
        List<FacilityResponse> facilities) {}
