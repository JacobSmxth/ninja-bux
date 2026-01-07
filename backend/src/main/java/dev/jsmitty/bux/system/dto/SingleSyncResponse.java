package dev.jsmitty.bux.system.dto;

public record SingleSyncResponse(String studentId, boolean updated, SyncChanges changes) {}
