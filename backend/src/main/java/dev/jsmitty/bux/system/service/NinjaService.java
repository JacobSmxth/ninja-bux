package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.CodeNinjasLoginRequest;
import dev.jsmitty.bux.system.dto.LocalSyncRequest;
import dev.jsmitty.bux.system.dto.NinjaListResponse;
import dev.jsmitty.bux.system.dto.NinjaResponse;
import dev.jsmitty.bux.system.dto.SingleSyncResponse;
import dev.jsmitty.bux.system.dto.SyncResponse;
import dev.jsmitty.bux.system.external.CodeNinjasApiClient;
import dev.jsmitty.bux.system.external.dto.CodeNinjasActivityResult;
import dev.jsmitty.bux.system.external.dto.CodeNinjasLoginResult;
import dev.jsmitty.bux.system.repository.NinjaRepository;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NinjaService {

  private static final Logger log = LoggerFactory.getLogger(NinjaService.class);
  private final NinjaRepository ninjaRepository;
  private final LedgerService ledgerService;
  private final PointCalculator pointCalculator;
  private final CodeNinjasApiClient codeNinjasApiClient;

  public NinjaService(
      NinjaRepository ninjaRepository,
      LedgerService ledgerService,
      PointCalculator pointCalculator,
      CodeNinjasApiClient codeNinjasApiClient) {
    this.ninjaRepository = ninjaRepository;
    this.ledgerService = ledgerService;
    this.pointCalculator = pointCalculator;
    this.codeNinjasApiClient = codeNinjasApiClient;
  }

  public NinjaListResponse getNinjas(UUID facilityId, int page, int size) {
    Page<Ninja> ninjaPage =
        ninjaRepository.findByFacilityId(facilityId, PageRequest.of(page, size));
    List<NinjaResponse> ninjas =
        ninjaPage.getContent().stream().map(NinjaResponse::summary).toList();
    return new NinjaListResponse(ninjas, ninjaPage.getTotalElements());
  }

  public Optional<NinjaResponse> getNinja(UUID facilityId, String studentId) {
    return ninjaRepository
        .findByFacilityIdAndStudentId(facilityId, studentId)
        .map(NinjaResponse::from);
  }

  @Transactional
  public SyncResponse syncAllNinjas(UUID facilityId) {
    // TODO: Implement actual Code Ninjas API integration
    // For now, return a placeholder response
    List<String> errors = new ArrayList<>();

    // This would normally fetch from Code Ninjas API
    // List<ExternalNinjaData> externalData = codeNinjasApiClient.fetchStudents(facilityId);

    return SyncResponse.facilitySync(0, 0, 0, errors);
  }

  @Transactional
  public SingleSyncResponse syncSingleNinja(UUID facilityId, String studentId) {
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST, "Use syncSingleNinja with login payload");
  }

  @Transactional
  public SingleSyncResponse syncSingleNinjaLocal(
      UUID facilityId, String studentId, LocalSyncRequest payload) {
    Map<String, Object> changes = upsertFromClientData(facilityId, studentId, payload);
    return new SingleSyncResponse(studentId, true, changes);
  }

  @Transactional
  public SingleSyncResponse syncSingleNinja(
      UUID facilityId, String studentId, CodeNinjasLoginRequest loginRequest) {
    CodeNinjasLoginResult login =
        codeNinjasApiClient.login(
            loginRequest.user(), loginRequest.latitude(), loginRequest.longitude());

    CodeNinjasActivityResult activity = codeNinjasApiClient.getCurrentActivity(login.token());

    // Fetch level status for step counts / sequence if available
    var levelStatus =
        activity.programId() != null && activity.courseId() != null
            ? codeNinjasApiClient.getLevelStatus(
                login.token(), activity.programId(), activity.courseId(), activity.levelId())
            : null;

    Integer activitySequence = null;
    if (levelStatus != null && levelStatus.activitySequences() != null && activity.activityId() != null) {
      activitySequence = levelStatus.activitySequences().get(activity.activityId());
      log.info("Activity ID: {}, Activity Sequences Map: {}, Extracted Sequence: {}",
          activity.activityId(), levelStatus.activitySequences(), activitySequence);
    } else {
      log.warn("Unable to extract activity sequence - levelStatus: {}, activityId: {}",
          levelStatus != null, activity.activityId());
    }

    LocalSyncRequest payload =
        new LocalSyncRequest(
            login.firstName(),
            login.lastName(),
            activity.courseName(),
            activity.levelId(),
            levelStatus != null ? levelStatus.levelSequence() : activity.levelSequence(),
            activity.activityId(),
            activitySequence,
            activity.groupId(),
            activity.subGroupId(),
            levelStatus != null ? levelStatus.completedSteps() : null,
            levelStatus != null ? levelStatus.totalSteps() : null,
            activity.lastModifiedDate());

    Map<String, Object> changes = upsertFromClientData(facilityId, studentId, payload);
    changes.put("token", login.token());
    changes.put("activity", activity);
    changes.put("levelStatus", levelStatus);

    return new SingleSyncResponse(studentId, true, changes);
  }

  @Transactional
  public Ninja createOrUpdateNinja(
      UUID facilityId,
      String studentId,
      String firstName,
      String lastName,
      String courseName,
      String levelName) {
    Optional<Ninja> existing = ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId);

    if (existing.isPresent()) {
      Ninja ninja = existing.get();

      ninja.setFirstName(firstName);
      ninja.setLastName(lastName);
      ninja.setCourseName(courseName);
      ninja.setLevelName(levelName);
      ninja.setLastSyncedAt(LocalDateTime.now());

      return ninjaRepository.save(ninja);
    } else {
      Ninja ninja = new Ninja(studentId, facilityId);
      ninja.setFirstName(firstName);
      ninja.setLastName(lastName);
      ninja.setCourseName(courseName);
      ninja.setLevelName(levelName);
      ninja.setLastSyncedAt(LocalDateTime.now());

      int initialBalance = pointCalculator.calculateInitialBalance(courseName, levelName);
      ninja.setCurrentBalance(initialBalance);

      Ninja saved = ninjaRepository.save(ninja);

      if (initialBalance > 0) {
        ledgerService.createTransaction(
            facilityId,
            studentId,
            initialBalance,
            TxnType.INITIAL_BALANCE,
            "Initial balance for " + courseName + " " + levelName,
            null);
      }

      return saved;
    }
  }

  public boolean ninjaExists(UUID facilityId, String studentId) {
    return ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId).isPresent();
  }

  private Map<String, Object> upsertFromClientData(
      UUID facilityId, String studentId, LocalSyncRequest payload) {
    Map<String, Object> changes = new HashMap<>();
    Optional<Ninja> existing = ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId);
    Ninja ninja = existing.orElseGet(() -> new Ninja(studentId, facilityId));

    // Store old values BEFORE updating for comparison
    Integer oldLevelSequence = ninja.getLevelSequence();
    Integer oldActivitySequence = ninja.getLastActivitySequence();

    log.info("Sync for student {}: oldLevelSeq={}, newLevelSeq={}, oldActivitySeq={}, newActivitySeq={}",
        studentId, oldLevelSequence, payload.levelSequence(), oldActivitySequence, payload.activitySequence());

    // Update ninja fields
    ninja.setFirstName(payload.firstName());
    ninja.setLastName(payload.lastName());
    ninja.setCourseName(payload.courseName());
    ninja.setLevelId(payload.levelId());
    ninja.setLevelSequence(payload.levelSequence());
    ninja.setLevelName(
        payload.levelSequence() != null ? "Level " + payload.levelSequence() : ninja.getLevelName());
    ninja.setActivityId(payload.activityId());
    ninja.setActivitySequence(payload.activitySequence());
    ninja.setGroupId(payload.groupId());
    ninja.setSubGroupId(payload.subGroupId());
    ninja.setCompletedSteps(payload.completedSteps());
    ninja.setTotalSteps(payload.totalSteps());
    ninja.setLastSyncedAt(LocalDateTime.now());
    ninja.setLastActivityId(payload.activityId());
    ninja.setLastActivitySequence(payload.activitySequence());
    if (payload.lastModifiedDate() != null) {
      ninja.setLastActivityUpdatedAt(payload.lastModifiedDate().toLocalDateTime());
    }

    boolean awarded = false;

    if (existing.isPresent()) {
      // Activity progression reward - when user advances to a new activity
      if (payload.activitySequence() != null && oldActivitySequence != null) {
        int currentSequence = payload.activitySequence();
        int activitiesCompleted = currentSequence - oldActivitySequence;

        if (activitiesCompleted > 0) {
          int bucksPerActivity = pointCalculator.getBeltMultiplier(payload.courseName());
          int totalReward = activitiesCompleted * bucksPerActivity;

          String description = activitiesCompleted == 1
              ? "Completed activity (seq " + oldActivitySequence + " → " + currentSequence + ")"
              : "Completed " + activitiesCompleted + " activities (seq " + oldActivitySequence + " → " + currentSequence + ")";

          ledgerService.createTransaction(
              facilityId,
              studentId,
              totalReward,
              TxnType.ACTIVITY_REWARD,
              description,
              null);
          awarded = true;
          changes.put("activityReward", totalReward);
          changes.put("activitiesCompleted", activitiesCompleted);
          log.info("Activity reward for {}: {} activities, {} bux", studentId, activitiesCompleted, totalReward);
        }
      }

      // Level progression reward - when user advances to a new level
      if (payload.levelSequence() != null && oldLevelSequence != null) {
        int levelDifference = payload.levelSequence() - oldLevelSequence;

        if (levelDifference > 0) {
          int progressionMultiplier = pointCalculator.getProgressionMultiplier(payload.courseName());
          int progressionReward = levelDifference * progressionMultiplier;

          String description = "Level progression: Level " + oldLevelSequence + " → " + payload.levelSequence();

          ledgerService.createTransaction(
              facilityId,
              studentId,
              progressionReward,
              TxnType.ACTIVITY_REWARD,
              description,
              null);

          changes.put("levelProgressionReward", progressionReward);
          changes.put("levelDifference", levelDifference);
          changes.put("oldLevelSequence", oldLevelSequence);
          changes.put("newLevelSequence", payload.levelSequence());
          awarded = true;
          log.info("Level progression reward for {}: {} levels, {} bux", studentId, levelDifference, progressionReward);
        }
      }
    } else {
      // New ninja - give initial balance
      int initialBalance =
          pointCalculator.calculateInitialBalance(
              payload.courseName(),
              payload.levelSequence() != null ? "Level " + payload.levelSequence() : null);
      ninja.setCurrentBalance(initialBalance);

      if (initialBalance > 0) {
        ledgerService.createTransaction(
            facilityId,
            studentId,
            initialBalance,
            TxnType.INITIAL_BALANCE,
            "Initial balance for " + payload.courseName() +
                (payload.levelSequence() != null ? " Level " + payload.levelSequence() : ""),
            null);
      }
      changes.put("initialBalance", initialBalance);
      log.info("New ninja {}: initial balance {} bux", studentId, initialBalance);
    }

    Ninja saved = ninjaRepository.save(ninja);
    changes.put("ninja", NinjaResponse.from(saved));
    if (!awarded) {
      changes.put("updated", true);
    }
    return changes;
  }
}
