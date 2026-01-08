package dev.jsmitty.bux.system.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Raw response payload from Code Ninjas current activity endpoint.
 *
 * <p>Parsed by {@link dev.jsmitty.bux.system.external.CodeNinjasApiClient} and then mapped to
 * {@link dev.jsmitty.bux.system.external.dto.CodeNinjasActivityResult}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CodeNinjasActivityResponse(
        String id,
        OffsetDateTime createdDate,
        OffsetDateTime lastModifiedDate,
        @JsonProperty("relationShips") Relationships relationShips) {

    /** Wrapper for nested relationship data in the activity payload. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Relationships(@JsonProperty("data") ActivityData data) {}

    /** Activity details embedded in the relationships section. */
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
