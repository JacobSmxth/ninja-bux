package dev.jsmitty.bux.system.dto;

import java.util.List;

/**
 * Response wrapper for listing purchases.
 */
public record PurchaseListResponse(List<PurchaseListItem> purchases) {}
