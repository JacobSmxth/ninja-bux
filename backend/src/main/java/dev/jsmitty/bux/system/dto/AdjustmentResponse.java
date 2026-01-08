package dev.jsmitty.bux.system.dto;

/**
 * Response payload for a created adjustment.
 */
public record AdjustmentResponse(Long adjustmentId, Integer newBalance, Long ledgerTxnId) {
    public static AdjustmentResponse created(
            Long adjustmentId, Integer newBalance, Long ledgerTxnId) {
        return new AdjustmentResponse(adjustmentId, newBalance, ledgerTxnId);
    }
}
