package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.NinjaListResponse;
import dev.jsmitty.bux.system.dto.NinjaResponse;
import dev.jsmitty.bux.system.dto.SingleSyncResponse;
import dev.jsmitty.bux.system.dto.SyncResponse;
import dev.jsmitty.bux.system.repository.NinjaRepository;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NinjaService {

  private final NinjaRepository ninjaRepository;
  private final LedgerService ledgerService;
  private final PointCalculator pointCalculator;

  public NinjaService(
      NinjaRepository ninjaRepository,
      LedgerService ledgerService,
      PointCalculator pointCalculator) {
    this.ninjaRepository = ninjaRepository;
    this.ledgerService = ledgerService;
    this.pointCalculator = pointCalculator;
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
    // TODO: Implement actual Code Ninjas API integration
    Map<String, Object> changes = new HashMap<>();

    return new SingleSyncResponse(studentId, false, changes);
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
}
