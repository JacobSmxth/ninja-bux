package dev.jsmitty.bux.system.dto;

import java.util.List;

public record NinjaListResponse(List<NinjaResponse> ninjas, long totalCount) {}
