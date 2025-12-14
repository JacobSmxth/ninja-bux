package dev.jsmitty.bux.system.dto;

import java.util.List;

public record LedgerResponse(List<LedgerTxnResponse> transactions, Integer currentBalance) {}
