package dev.jsmitty.bux.system.external.dto;

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
    Integer nextSequence) {}
