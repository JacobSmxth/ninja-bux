package dev.jsmitty.bux.system.dto;

import java.util.List;

/**
 * Response wrapper for listing admins.
 */
public record AdminListResponse(List<AdminResponse> admins, long totalCount) {}
