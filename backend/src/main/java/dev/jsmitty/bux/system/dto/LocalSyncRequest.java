package dev.jsmitty.bux.system.dto;

import java.time.OffsetDateTime;

/**
 * Local/offline sync payload for a ninja's activity and progress.
 *
 * <p>Used by {@link dev.jsmitty.bux.system.service.NinjaService} when the client already has
 * activity data and wants to persist it without calling Code Ninjas.
 */
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
        OffsetDateTime lastModifiedDate,
        String facilityName) {}
