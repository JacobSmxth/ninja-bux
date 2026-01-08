package dev.jsmitty.bux.system.domain;

/**
 * State machine for shop purchases.
 *
 * <p>Used by {@link dev.jsmitty.bux.system.service.PurchaseService} to control fulfillment
 * and cancellation flows.
 */
public enum PurchaseStatus {
    PENDING,
    FULFILLED,
    CANCELLED
}
