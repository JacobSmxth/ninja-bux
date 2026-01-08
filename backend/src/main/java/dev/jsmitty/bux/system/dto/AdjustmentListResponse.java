package dev.jsmitty.bux.system.dto;

import java.util.List;

/**
 * Response wrapper for listing adjustments.
 */
public record AdjustmentListResponse(List<AdjustmentListItem> adjustments) {}
