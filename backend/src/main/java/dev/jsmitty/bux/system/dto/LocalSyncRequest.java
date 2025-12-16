package dev.jsmitty.bux.system.dto;

import java.time.OffsetDateTime;

public record LocalSyncRequest(
    String firstName,
    String lastName,
    String courseName,
    String levelId,
    Integer levelSequence,
    String activityId,
    Integer activitySequence,
    String groupId,
    String subGroupId,
    Integer completedSteps,
    Integer totalSteps,
    OffsetDateTime lastModifiedDate) {}
