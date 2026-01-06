package dev.jsmitty.bux.system.dto;

public record AdjustmentResponse(Long adjustmentId, Integer newBalance, Long ledgerTxnId) {
    public static AdjustmentResponse created(
            Long adjustmentId, Integer newBalance, Long ledgerTxnId) {
        return new AdjustmentResponse(adjustmentId, newBalance, ledgerTxnId);
    }
}
