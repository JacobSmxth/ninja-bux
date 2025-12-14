package dev.jsmitty.bux.system.external.dto;

import java.time.OffsetDateTime;

public record CodeNinjasActivityResult(
    String userActivityId,
    String activityId,
    String studentId,
    String programId,
    String courseId,
    String courseName,
    String levelName,
    String levelId,
    Integer levelSequence,
    String activityName,
    String activityType,
    String groupId,
    String subGroupId,
    OffsetDateTime createdDate,
    OffsetDateTime lastModifiedDate) {}
