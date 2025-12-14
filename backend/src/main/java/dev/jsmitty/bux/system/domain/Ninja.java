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

  @Column(name = "activity_name")
  private String activityName;

  @Column(name = "activity_type")
  private String activityType;

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

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  public String getActivityType() {
    return activityType;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
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
    return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
  }
}
