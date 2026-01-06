package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.domain.Admin;

import java.time.LocalDateTime;
import java.util.List;

public record AdminResponse(
        Long id,
        String username,
        String email,
        boolean superAdmin,
        List<FacilityResponse> facilities,
        LocalDateTime createdAt) {
    public static AdminResponse from(Admin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.isSuperAdmin(),
                admin.getFacilities().stream().map(FacilityResponse::from).toList(),
                admin.getCreatedAt());
    }
}
