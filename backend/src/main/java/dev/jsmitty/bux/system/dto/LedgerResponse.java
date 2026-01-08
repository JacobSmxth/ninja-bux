package dev.jsmitty.bux.system.dto;

import java.util.List;

/**
 * Ledger response including transactions and current balance snapshot.
 */
public record LedgerResponse(List<LedgerTxnResponse> transactions, Integer currentBalance) {}
