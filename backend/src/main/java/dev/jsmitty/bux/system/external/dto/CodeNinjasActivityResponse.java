package dev.jsmitty.bux.system.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeNinjasActivityResponse(
    String id,
    OffsetDateTime createdDate,
    OffsetDateTime lastModifiedDate,
    @JsonProperty("relationShips") Relationships relationShips) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Relationships(@JsonProperty("data") ActivityData data) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ActivityData(
      String studentId,
      String programId,
      String courseId,
      String levelId,
      String activityId,
      String groupId,
      String subgroupId,
      String firstName,
      String lastName,
      String courseName,
      String activityName,
      String programName,
      String levelName,
      String activityType) {}
}
