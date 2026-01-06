package dev.jsmitty.bux.system.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ninjas",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "facility_id"})})
public class Ninja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "level_name")
    private String levelName;

    @Column(name = "level_id")
    private String levelId;

    @Column(name = "level_sequence")
    private Integer levelSequence;

    @Column(name = "activity_id")
    private String activityId;

    @Column(name = "activity_sequence")
    private Integer activitySequence;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "sub_group_id")
    private String subGroupId;

    @Column(name = "completed_steps")
    private Integer completedSteps;

    @Column(name = "total_steps")
    private Integer totalSteps;

    @Column(name = "last_activity_id")
    private String lastActivityId;

    @Column(name = "last_activity_sequence")
    private Integer lastActivitySequence;

    @Column(name = "last_activity_updated_at")
    private LocalDateTime lastActivityUpdatedAt;

    @Column(name = "current_balance", nullable = false)
    private Integer currentBalance = 0;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Ninja() {}

    public Ninja(String studentId, UUID facilityId) {
        this.studentId = studentId;
        this.facilityId = facilityId;
        this.currentBalance = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public UUID getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(UUID facilityId) {
        this.facilityId = facilityId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public Integer getLevelSequence() {
        return levelSequence;
    }

    public void setLevelSequence(Integer levelSequence) {
        this.levelSequence = levelSequence;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Integer getActivitySequence() {
        return activitySequence;
    }

    public void setActivitySequence(Integer activitySequence) {
        this.activitySequence = activitySequence;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSubGroupId() {
        return subGroupId;
    }

    public void setSubGroupId(String subGroupId) {
        this.subGroupId = subGroupId;
    }

    public Integer getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(Integer completedSteps) {
        this.completedSteps = completedSteps;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getLastActivityId() {
        return lastActivityId;
    }

    public void setLastActivityId(String lastActivityId) {
        this.lastActivityId = lastActivityId;
    }

    public Integer getLastActivitySequence() {
        return lastActivitySequence;
    }

    public void setLastActivitySequence(Integer lastActivitySequence) {
        this.lastActivitySequence = lastActivitySequence;
    }

    public LocalDateTime getLastActivityUpdatedAt() {
        return lastActivityUpdatedAt;
    }

    public void setLastActivityUpdatedAt(LocalDateTime lastActivityUpdatedAt) {
        this.lastActivityUpdatedAt = lastActivityUpdatedAt;
    }

    public Integer getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Integer currentBalance) {
        this.currentBalance = currentBalance;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""))
                .trim();
    }
}
