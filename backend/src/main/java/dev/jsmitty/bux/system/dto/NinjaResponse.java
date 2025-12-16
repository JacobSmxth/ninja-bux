package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.domain.Ninja;
import java.time.LocalDateTime;

public record NinjaResponse(
    Long id,
    String studentId,
    String firstName,
    String lastName,
    String courseName,
    String levelName,
    Integer levelSequence,
    String activityId,
    Integer activitySequence,
    String groupId,
    String subGroupId,
    Integer completedSteps,
    Integer totalSteps,
    Integer currentBalance,
    LocalDateTime lastSyncedAt) {
  public static NinjaResponse from(Ninja ninja) {
    return new NinjaResponse(
        ninja.getId(),
        ninja.getStudentId(),
        ninja.getFirstName(),
        ninja.getLastName(),
        ninja.getCourseName(),
        ninja.getLevelName(),
        ninja.getLevelSequence(),
        ninja.getActivityId(),
        ninja.getActivitySequence(),
        ninja.getGroupId(),
        ninja.getSubGroupId(),
        ninja.getCompletedSteps(),
        ninja.getTotalSteps(),
        ninja.getCurrentBalance(),
        ninja.getLastSyncedAt());
  }

  public static NinjaResponse summary(Ninja ninja) {
    return from(ninja);
  }
}
