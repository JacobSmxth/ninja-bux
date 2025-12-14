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
    String activityName,
    String activityType,
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
        ninja.getActivityName(),
        ninja.getActivityType(),
        ninja.getCurrentBalance(),
        ninja.getLastSyncedAt());
  }

  public static NinjaResponse summary(Ninja ninja) {
    return new NinjaResponse(
        ninja.getId(),
        ninja.getStudentId(),
        ninja.getFirstName(),
        ninja.getLastName(),
        ninja.getCourseName(),
        ninja.getLevelName(),
        null,
        null,
        ninja.getCurrentBalance(),
        ninja.getLastSyncedAt());
  }
}
