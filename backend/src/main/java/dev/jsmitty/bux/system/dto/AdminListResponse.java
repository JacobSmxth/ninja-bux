package dev.jsmitty.bux.system.dto;

import java.util.List;

public record AdminListResponse(
    List<AdminResponse> admins,
    long totalCount
) {}
