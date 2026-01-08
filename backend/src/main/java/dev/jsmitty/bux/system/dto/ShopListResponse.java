package dev.jsmitty.bux.system.dto;

import java.util.List;

/**
 * Response wrapper for listing shop items.
 */
public record ShopListResponse(List<ShopItemResponse> items) {}
