package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.Facility;
import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.CodeNinjasLoginRequest;
import dev.jsmitty.bux.system.dto.LocalSyncRequest;
import dev.jsmitty.bux.system.dto.NinjaListResponse;
import dev.jsmitty.bux.system.dto.NinjaResponse;
import dev.jsmitty.bux.system.dto.SingleSyncResponse;
import dev.jsmitty.bux.system.dto.SyncChanges;
import dev.jsmitty.bux.system.external.CodeNinjasApiClient;
import dev.jsmitty.bux.system.external.dto.CodeNinjasActivityResult;
import dev.jsmitty.bux.system.external.dto.CodeNinjasLoginResult;
import dev.jsmitty.bux.system.external.dto.LevelStatusSummary;
import dev.jsmitty.bux.system.repository.FacilityRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class NinjaService {

    private static final Logger log = LoggerFactory.getLogger(NinjaService.class);
    private static final int MAX_PAGE_SIZE = 100;
    private static final LocalDateTime INITIAL_BALANCE_CREATED_AT =
            LocalDateTime.of(1970, 1, 1, 0, 0);

    private final NinjaRepository ninjaRepository;
    private final FacilityRepository facilityRepository;
    private final LedgerService ledgerService;
    private final PointCalculator pointCalculator;
    private final CodeNinjasApiClient codeNinjasApiClient;

    public NinjaService(
            NinjaRepository ninjaRepository,
            FacilityRepository facilityRepository,
            LedgerService ledgerService,
            PointCalculator pointCalculator,
            CodeNinjasApiClient codeNinjasApiClient) {
        this.ninjaRepository = ninjaRepository;
        this.facilityRepository = facilityRepository;
        this.ledgerService = ledgerService;
        this.pointCalculator = pointCalculator;
        this.codeNinjasApiClient = codeNinjasApiClient;
    }

    // ==================== PUBLIC API (Read Operations) ====================

    @Transactional(readOnly = true)
    public NinjaListResponse getNinjas(UUID facilityId, Pageable pageable) {
        Pageable effective = withDefaultSortAndLimits(pageable);

        Page<NinjaResponse> page =
                ninjaRepository.findByFacilityId(facilityId, effective).map(NinjaResponse::summary);

        return NinjaListResponse.from(page);
    }

    private Pageable withDefaultSortAndLimits(Pageable pageable) {
        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);

        Sort sort =
                pageable.getSort().isSorted()
                        ? pageable.getSort()
                        : Sort.by(Sort.Direction.ASC, "id");

        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }

    @Transactional(readOnly = true)
    public Optional<NinjaResponse> getNinja(UUID facilityId, String studentId) {
        return ninjaRepository
                .findByFacilityIdAndStudentId(facilityId, studentId)
                .map(NinjaResponse::from);
    }

    @Transactional(readOnly = true)
    public boolean ninjaExists(UUID facilityId, String studentId) {
        return ninjaRepository.existsByFacilityIdAndStudentId(facilityId, studentId);
    }

    // ==================== PUBLIC API (Sync Operations) ====================

    /**
     * Remote sync: calls external Code Ninjas API, then persists.
     * NO @Transactional here — network calls must NOT hold DB connections open.
     */
    public SingleSyncResponse syncSingleNinja(
            UUID facilityId, String studentId, CodeNinjasLoginRequest loginRequest) {
        validateSyncInput(facilityId, studentId);
        Objects.requireNonNull(loginRequest, "loginRequest must not be null");

        // Phase 1: External API calls (NO transaction)
        CodeNinjasLoginResult login =
                codeNinjasApiClient.login(
                        loginRequest.user(), loginRequest.latitude(), loginRequest.longitude());

        CodeNinjasActivityResult activity = codeNinjasApiClient.getCurrentActivity(login.token());

        LevelStatusSummary levelStatus = fetchLevelStatusIfAvailable(login.token(), activity);
        Integer activitySequence = extractActivitySequence(activity, levelStatus);

        // Phase 2: Build payload from remote data
        LocalSyncRequest payload =
                buildPayloadFromRemoteData(login, activity, levelStatus, activitySequence);

        // Phase 3: Persist with transaction (atomic, locked)
        SyncChanges changes = persistSyncWithLock(facilityId, studentId, payload);

        // Phase 4: Enrich response with remote-only data
        SyncChanges enriched = enrichWithRemoteData(changes, login.token(), activity, levelStatus);

        return new SingleSyncResponse(studentId, true, enriched);
    }

    private void validateSyncInput(UUID facilityId, String studentId) {
        Objects.requireNonNull(facilityId, "facilityId must not be null");
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId must not be blank");
        }
    }

    private LevelStatusSummary fetchLevelStatusIfAvailable(
            String token, CodeNinjasActivityResult activity) {
        if (activity.programId() != null && activity.courseId() != null) {
            return codeNinjasApiClient.getLevelStatus(
                    token, activity.programId(), activity.courseId(), activity.levelId());
        }
        return null;
    }

    private Integer extractActivitySequence(
            CodeNinjasActivityResult activity, LevelStatusSummary levelStatus) {
        if (levelStatus != null
                && levelStatus.activitySequences() != null
                && activity.activityId() != null) {
            Integer sequence = levelStatus.activitySequences().get(activity.activityId());
            log.debug(
                    "Activity ID: {}, Activity Sequences Map: {}, Extracted Sequence: {}",
                    activity.activityId(),
                    levelStatus.activitySequences(),
                    sequence);
            return sequence;
        }
        log.debug(
                "Unable to extract activity sequence - levelStatus: {}, activityId: {}",
                levelStatus != null,
                activity.activityId());
        return null;
    }

    private LocalSyncRequest buildPayloadFromRemoteData(
            CodeNinjasLoginResult login,
            CodeNinjasActivityResult activity,
            LevelStatusSummary levelStatus,
            Integer activitySequence) {
        return new LocalSyncRequest(
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
                activity.lastModifiedDate(),
                login.facilityName());
    }

    private SyncChanges enrichWithRemoteData(
            SyncChanges base,
            String token,
            CodeNinjasActivityResult activity,
            LevelStatusSummary levelStatus) {
        return SyncChanges.builder()
                .ninja(base.ninja())
                .initialBalance(base.initialBalance())
                .activityReward(base.activityReward())
                .activitiesCompleted(base.activitiesCompleted())
                .levelProgressionReward(base.levelProgressionReward())
                .levelDifference(base.levelDifference())
                .oldLevelSequence(base.oldLevelSequence())
                .newLevelSequence(base.newLevelSequence())
                .updated(base.updated())
                .token(token)
                .activity(activity)
                .levelStatus(levelStatus)
                .build();
    }

    /**
     * Local sync: persists client-provided data directly.
     */
    @Transactional
    public SingleSyncResponse syncSingleNinjaLocal(
            UUID facilityId, String studentId, LocalSyncRequest payload) {
        validateSyncInput(facilityId, studentId);
        Objects.requireNonNull(payload, "payload must not be null");

        ensureFacilityExists(facilityId, payload);

        SyncChanges changes = persistSyncWithLock(facilityId, studentId, payload);

        return new SingleSyncResponse(studentId, true, changes);
    }

    // ==================== FACILITY MANAGEMENT ====================

    private Facility ensureFacilityExists(UUID facilityId, LocalSyncRequest payload) {
        return facilityRepository
                .findById(facilityId)
                .orElseGet(() -> createFacilityWithRaceHandling(facilityId, payload));
    }

    private Facility createFacilityWithRaceHandling(UUID facilityId, LocalSyncRequest payload) {
        String facilityName = defaultFacilityName(facilityId, payload);

        try {
            Facility created = facilityRepository.save(new Facility(facilityId, facilityName));
            log.info("Auto-created facility: {} ({})", facilityName, facilityId);
            return created;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Another request created it between our read and save
            return facilityRepository.findById(facilityId).orElseThrow(() -> e);
        }
    }

    private String defaultFacilityName(UUID facilityId, LocalSyncRequest payload) {
        String name = payload.facilityName();
        if (name == null || name.isBlank()) {
            return "Facility " + facilityId.toString().substring(0, 8);
        }
        return name.trim();
    }

    // ==================== TRANSACTIONAL PERSISTENCE (with lock) ====================

    /**
     * Core persistence logic with pessimistic locking to prevent double-awards.
     * Called by both local and remote sync paths.
     *
     * Uses SELECT ... FOR UPDATE to lock the ninja row during the entire
     * read-compute-award-write cycle, preventing concurrent sync requests
     * from computing rewards based on stale sequence values.
     */
    @Transactional
    protected SyncChanges persistSyncWithLock(
            UUID facilityId, String studentId, LocalSyncRequest payload) {
        // Use FOR UPDATE to lock the row and prevent concurrent award calculations
        Optional<Ninja> existing =
                ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId);

        return existing.isPresent()
                ? updateExistingNinja(facilityId, studentId, existing.get(), payload)
                : createNewNinja(facilityId, studentId, payload);
    }

    private SyncChanges updateExistingNinja(
            UUID facilityId, String studentId, Ninja ninja, LocalSyncRequest payload) {
        SyncChanges.Builder changes = SyncChanges.builder();

        // Capture old values BEFORE updating for delta calculation
        Integer oldLevelSequence = ninja.getLevelSequence();
        Integer oldActivitySequence = ninja.getLastActivitySequence();

        log.debug(
                "Sync for student {}: oldLevelSeq={}, newLevelSeq={}, oldActivitySeq={}, newActivitySeq={}",
                studentId,
                oldLevelSequence,
                payload.levelSequence(),
                oldActivitySequence,
                payload.activitySequence());

        // Update ninja fields
        applyPayloadToNinja(ninja, payload);

        // Calculate and award progression rewards
        boolean activityAwarded =
                awardActivityProgression(facilityId, studentId, payload, oldActivitySequence, changes);
        boolean levelAwarded =
                awardLevelProgression(facilityId, studentId, payload, oldLevelSequence, changes);

        Ninja saved = ninjaRepository.save(ninja);
        changes.ninja(NinjaResponse.from(saved));
        changes.updated(!activityAwarded && !levelAwarded);

        return changes.build();
    }

    private SyncChanges createNewNinja(
            UUID facilityId, String studentId, LocalSyncRequest payload) {
        SyncChanges.Builder changes = SyncChanges.builder();

        Ninja ninja = new Ninja(studentId, facilityId);
        applyPayloadToNinja(ninja, payload);

        int initialBalance =
                pointCalculator.calculateInitialBalance(
                        payload.courseName(),
                        payload.levelSequence() != null ? "Level " + payload.levelSequence() : null);
        ninja.setCurrentBalance(initialBalance);

        Ninja saved = ninjaRepository.save(ninja);

        if (initialBalance > 0) {
            ledgerService.createTransaction(
                    facilityId,
                    studentId,
                    initialBalance,
                    TxnType.INITIAL_BALANCE,
                    "Initial balance for "
                            + payload.courseName()
                            + (payload.levelSequence() != null
                                    ? " Level " + payload.levelSequence()
                                    : ""),
                    null,
                    INITIAL_BALANCE_CREATED_AT);
        }

        changes.ninja(NinjaResponse.from(saved));
        changes.initialBalance(initialBalance);
        log.info("New ninja {}: initial balance {} bux", studentId, initialBalance);

        return changes.build();
    }

    private void applyPayloadToNinja(Ninja ninja, LocalSyncRequest payload) {
        ninja.setFirstName(payload.firstName());
        ninja.setLastName(payload.lastName());
        ninja.setCourseName(payload.courseName());
        ninja.setLevelId(payload.levelId());
        ninja.setLevelSequence(payload.levelSequence());
        ninja.setLevelName(
                payload.levelSequence() != null
                        ? "Level " + payload.levelSequence()
                        : ninja.getLevelName());
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
    }

    // ==================== REWARD CALCULATION ====================

    /**
     * Awards bux for activity progression (completing activities within a level).
     *
     * @return true if a reward was awarded
     */
    private boolean awardActivityProgression(
            UUID facilityId,
            String studentId,
            LocalSyncRequest payload,
            Integer oldActivitySequence,
            SyncChanges.Builder changes) {

        if (payload.activitySequence() == null || oldActivitySequence == null) {
            return false;
        }

        int activitiesCompleted = payload.activitySequence() - oldActivitySequence;
        if (activitiesCompleted <= 0) {
            return false;
        }

        int bucksPerActivity = pointCalculator.getBeltMultiplier(payload.courseName());
        int totalReward = activitiesCompleted * bucksPerActivity;

        String description =
                activitiesCompleted == 1
                        ? "Completed activity (seq "
                                + oldActivitySequence
                                + " → "
                                + payload.activitySequence()
                                + ")"
                        : "Completed "
                                + activitiesCompleted
                                + " activities (seq "
                                + oldActivitySequence
                                + " → "
                                + payload.activitySequence()
                                + ")";

        ledgerService.createTransaction(
                facilityId, studentId, totalReward, TxnType.ACTIVITY_REWARD, description, null);

        changes.activityReward(totalReward);
        changes.activitiesCompleted(activitiesCompleted);
        log.info(
                "Activity reward for {}: {} activities, {} bux",
                studentId,
                activitiesCompleted,
                totalReward);

        return true;
    }

    /**
     * Awards bux for level progression (advancing to a higher level).
     *
     * @return true if a reward was awarded
     */
    private boolean awardLevelProgression(
            UUID facilityId,
            String studentId,
            LocalSyncRequest payload,
            Integer oldLevelSequence,
            SyncChanges.Builder changes) {

        if (payload.levelSequence() == null || oldLevelSequence == null) {
            return false;
        }

        int levelDifference = payload.levelSequence() - oldLevelSequence;
        if (levelDifference <= 0) {
            return false;
        }

        int progressionMultiplier = pointCalculator.getProgressionMultiplier(payload.courseName());
        int progressionReward = levelDifference * progressionMultiplier;

        String description =
                "Level progression: Level " + oldLevelSequence + " → " + payload.levelSequence();

        ledgerService.createTransaction(
                facilityId, studentId, progressionReward, TxnType.ACTIVITY_REWARD, description, null);

        changes.levelProgressionReward(progressionReward);
        changes.levelDifference(levelDifference);
        changes.oldLevelSequence(oldLevelSequence);
        changes.newLevelSequence(payload.levelSequence());
        log.info(
                "Level progression reward for {}: {} levels, {} bux",
                studentId,
                levelDifference,
                progressionReward);

        return true;
    }
}
