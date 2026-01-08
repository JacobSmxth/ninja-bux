package dev.jsmitty.bux.system.domain;

/**
 * Ledger transaction categories for reporting and balance calculations.
 *
 * <p>Used throughout {@link dev.jsmitty.bux.system.service.LedgerService},
 * {@link dev.jsmitty.bux.system.service.NinjaService}, and leaderboard queries.
 */
public enum TxnType {
    INITIAL_BALANCE,
    ACTIVITY_REWARD,
    PURCHASE,
    ADJUSTMENT
}
