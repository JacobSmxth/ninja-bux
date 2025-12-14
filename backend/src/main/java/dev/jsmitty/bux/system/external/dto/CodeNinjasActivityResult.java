package dev.jsmitty.bux.system.external.dto;

import java.time.OffsetDateTime;

public record CodeNinjasActivityResult(
    String userActivityId,
    String activityId,
    String studentId,
    String courseName,
    String levelName,
    String activityName,
    String activityType,
    OffsetDateTime createdDate,
    OffsetDateTime lastModifiedDate) {}
