package dev.jsmitty.bux.system.dto;

import java.time.OffsetDateTime;

public record LocalSyncRequest(
    String firstName,
    String lastName,
    String courseName,
    String levelName,
    String activityName,
    String activityType,
    String activityId,
    OffsetDateTime lastModifiedDate) {}
