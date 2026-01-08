package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.domain.Facility;

import java.util.UUID;

/**
 * API projection of a facility.
 */
public record FacilityResponse(UUID id, String name) {
    public static FacilityResponse from(Facility facility) {
        return new FacilityResponse(facility.getId(), facility.getName());
    }
}
