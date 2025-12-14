package dev.jsmitty.bux.system.dto;

import java.util.List;

public record SyncResponse(int syncedCount, int newNinjas, int updatedNinjas, List<String> errors) {
  public static SyncResponse facilitySync(
      int synced, int newCount, int updated, List<String> errors) {
    return new SyncResponse(synced, newCount, updated, errors);
  }
}
