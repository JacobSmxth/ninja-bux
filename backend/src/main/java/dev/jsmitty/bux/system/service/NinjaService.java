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

@Service
public class NinjaService {

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

    Map<String, Object> changes =
        upsertFromActivity(facilityId, studentId, login, activity);
    changes.put("token", login.token());
    changes.put("activity", activity);

    return new SingleSyncResponse(studentId, true, changes);
  }

  @Transactional
  public Ninja createOrUpdateNinja(
      UUID facilityId,
      String studentId,
      String firstName,
      String lastName,
      String courseName,
      String levelName,
      String activityName,
      String activityType) {
    Optional<Ninja> existing = ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId);

    if (existing.isPresent()) {
      Ninja ninja = existing.get();
      String oldActivityName = ninja.getActivityName();

      ninja.setFirstName(firstName);
      ninja.setLastName(lastName);
      ninja.setCourseName(courseName);
      ninja.setLevelName(levelName);
      ninja.setActivityType(activityType);
      ninja.setLastSyncedAt(LocalDateTime.now());

      if (activityName != null && !activityName.equals(oldActivityName)) {
        ninja.setActivityName(activityName);
        int reward = pointCalculator.calculateActivityReward(courseName, activityType);
        if (reward > 0) {
          ledgerService.createTransaction(
              facilityId,
              studentId,
              reward,
              TxnType.ACTIVITY_REWARD,
              "Completed: " + activityName,
              null);
        }
      }

      return ninjaRepository.save(ninja);
    } else {
      Ninja ninja = new Ninja(studentId, facilityId);
      ninja.setFirstName(firstName);
      ninja.setLastName(lastName);
      ninja.setCourseName(courseName);
      ninja.setLevelName(levelName);
      ninja.setActivityName(activityName);
      ninja.setActivityType(activityType);
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

  private Map<String, Object> upsertFromActivity(
      UUID facilityId,
      String studentId,
      CodeNinjasLoginResult login,
      CodeNinjasActivityResult activity) {
    Map<String, Object> changes = new HashMap<>();

    Optional<Ninja> existing = ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId);
    Ninja ninja = existing.orElseGet(() -> new Ninja(studentId, facilityId));

    ninja.setFirstName(login.firstName());
    ninja.setLastName(login.lastName());
    ninja.setCourseName(activity.courseName());
    ninja.setLevelName(activity.levelName());
    ninja.setActivityName(activity.activityName());
    ninja.setActivityType(activity.activityType());
    ninja.setLastSyncedAt(LocalDateTime.now());

    boolean newActivity =
        activity.activityId() != null
            && !activity.activityId().equals(ninja.getLastActivityId());

    boolean awarded = false;
    int reward = 0;

    if (existing.isPresent()) {
      if (newActivity && activity.activityName() != null) {
        reward = pointCalculator.calculateActivityReward(activity.courseName(), activity.activityType());
        if (reward > 0) {
          ledgerService.createTransaction(
              facilityId,
              studentId,
              reward,
              TxnType.ACTIVITY_REWARD,
              "Completed: " + activity.activityName(),
              null);
          awarded = true;
          changes.put("rewardPoints", reward);
          changes.put("activityName", activity.activityName());
        }
      }
    } else {
      int initialBalance =
          pointCalculator.calculateInitialBalance(activity.courseName(), activity.levelName());
      ninja.setCurrentBalance(initialBalance);
      ninjaRepository.save(ninja);
      if (initialBalance > 0) {
        ledgerService.createTransaction(
            facilityId,
            studentId,
            initialBalance,
            TxnType.INITIAL_BALANCE,
            "Initial balance for " + activity.courseName() + " " + activity.levelName(),
            null);
      }
      changes.put("initialBalance", initialBalance);
    }

    ninja.setLastActivityId(activity.activityId());
    if (activity.lastModifiedDate() != null) {
      ninja.setLastActivityUpdatedAt(activity.lastModifiedDate().toLocalDateTime());
    }

    Ninja saved = ninjaRepository.save(ninja);

    changes.put("ninja", NinjaResponse.from(saved));
    if (!awarded) {
      changes.put("updated", true);
    }

    return changes;
  }

  private Map<String, Object> upsertFromClientData(
      UUID facilityId, String studentId, LocalSyncRequest payload) {
    Map<String, Object> changes = new HashMap<>();
    Optional<Ninja> existing = ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId);
    Ninja ninja = existing.orElseGet(() -> new Ninja(studentId, facilityId));

    ninja.setFirstName(payload.firstName());
    ninja.setLastName(payload.lastName());
    ninja.setCourseName(payload.courseName());
    ninja.setLevelName(payload.levelName());
    ninja.setActivityName(payload.activityName());
    ninja.setActivityType(payload.activityType());
    ninja.setLastSyncedAt(LocalDateTime.now());

    boolean newActivity = false;
    if (payload.activityId() != null && !payload.activityId().isBlank()) {
      newActivity = !payload.activityId().equals(ninja.getLastActivityId());
    } else if (payload.activityName() != null
        && !payload.activityName().equals(ninja.getActivityName())) {
      newActivity = true;
    }

    boolean awarded = false;
    if (existing.isPresent()) {
      if (newActivity && payload.activityName() != null) {
        int reward =
            pointCalculator.calculateActivityReward(payload.courseName(), payload.activityType());
        if (reward > 0) {
          ledgerService.createTransaction(
              facilityId,
              studentId,
              reward,
              TxnType.ACTIVITY_REWARD,
              "Completed: " + payload.activityName(),
              null);
          awarded = true;
          changes.put("rewardPoints", reward);
          changes.put("activityName", payload.activityName());
        }
      }
    } else {
      int initialBalance =
          pointCalculator.calculateInitialBalance(payload.courseName(), payload.levelName());
      ninja.setCurrentBalance(initialBalance);
      Ninja saved = ninjaRepository.save(ninja);
      if (initialBalance > 0) {
        ledgerService.createTransaction(
            facilityId,
            studentId,
            initialBalance,
            TxnType.INITIAL_BALANCE,
            "Initial balance for " + payload.courseName() + " " + payload.levelName(),
            null);
      }
      changes.put("initialBalance", initialBalance);
      ninja = saved;
    }

    ninja.setLastActivityId(payload.activityId());
    if (payload.lastModifiedDate() != null) {
      ninja.setLastActivityUpdatedAt(payload.lastModifiedDate().toLocalDateTime());
    }

    Ninja saved = ninjaRepository.save(ninja);
    changes.put("ninja", NinjaResponse.from(saved));
    if (!awarded) {
      changes.put("updated", true);
    }
    return changes;
  }
}
