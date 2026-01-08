package dev.jsmitty.bux.system.external.dto;

import java.time.OffsetDateTime;

/**
 * Normalized activity data used by sync logic.
 *
 * <p>Produced by {@link dev.jsmitty.bux.system.external.CodeNinjasApiClient} and consumed by
 * {@link dev.jsmitty.bux.system.service.NinjaService}.
 */
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
