package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.domain.LedgerTxn;
import dev.jsmitty.bux.system.domain.TxnType;

import java.time.LocalDateTime;

public record LedgerTxnResponse(
        Long id, Integer amount, TxnType type, String description, LocalDateTime createdAt) {
    public static LedgerTxnResponse from(LedgerTxn txn) {
        return new LedgerTxnResponse(
                txn.getId(),
                txn.getAmount(),
                txn.getType(),
                txn.getDescription(),
                txn.getCreatedAt());
    }
}
