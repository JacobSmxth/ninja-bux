package dev.jsmitty.bux.system.external.dto;

import java.util.Map;

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
