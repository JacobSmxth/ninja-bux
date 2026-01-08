package dev.jsmitty.bux.system.dto;

/**
 * Response payload for a single-ninja sync call.
 */
public record SingleSyncResponse(String studentId, boolean updated, SyncChanges changes) {}
