package dev.jsmitty.bux.system.external.dto;

import java.util.Map;

/**
 * Summarized level completion data from Code Ninjas.
 *
 * <p>Used by sync logic to calculate step progression rewards.
 */
public record LevelStatusSummary(
        String programId,
        String courseId,
        String levelId,
        Integer levelSequence,
        int totalSteps,
        int completedSteps,
        int completionPercent,
        String nextActivityId,
        String nextActivityType,
        Integer nextSequence,
        Map<String, Integer> activitySequences) {}
