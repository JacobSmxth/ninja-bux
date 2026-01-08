package dev.jsmitty.bux.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.jsmitty.bux.system.domain.Facility;
import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.dto.LocalSyncRequest;
import dev.jsmitty.bux.system.dto.SingleSyncResponse;
import dev.jsmitty.bux.system.external.CodeNinjasApiClient;
import dev.jsmitty.bux.system.repository.FacilityRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class NinjaServiceTest {

    @Mock NinjaRepository ninjaRepository;
    @Mock FacilityRepository facilityRepository;
    @Mock LedgerService ledgerService;
    @Mock PointCalculator pointCalculator;
    @Mock CodeNinjasApiClient codeNinjasApiClient;

    @InjectMocks NinjaService ninjaService;

    // ==================== Pagination Tests ====================

    @Test
    void getNinjas_capsPageSizeAndAppliesDefaultSort() {
        UUID facilityId = UUID.randomUUID();

        Pageable requested = PageRequest.of(0, 500); // too large, no sort

        Page<Ninja> repoPage = new PageImpl<>(List.of(), requested, 0);

        when(ninjaRepository.findByFacilityId(eq(facilityId), any(Pageable.class)))
                .thenReturn(repoPage);

        ninjaService.getNinjas(facilityId, requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(ninjaRepository).findByFacilityId(eq(facilityId), captor.capture());

        Pageable effective = captor.getValue();
        assertThat(effective.getPageSize()).isEqualTo(100);
        assertThat(effective.getSort().isSorted()).isTrue();
        assertThat(effective.getSort().getOrderFor("id").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getNinjas_preservesUserProvidedSort() {
        UUID facilityId = UUID.randomUUID();
        Sort userSort = Sort.by(Sort.Direction.DESC, "lastName");
        Pageable requested = PageRequest.of(0, 50, userSort);

        Page<Ninja> repoPage = new PageImpl<>(List.of(), requested, 0);
        when(ninjaRepository.findByFacilityId(eq(facilityId), any(Pageable.class)))
                .thenReturn(repoPage);

        ninjaService.getNinjas(facilityId, requested);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(ninjaRepository).findByFacilityId(eq(facilityId), captor.capture());

        Pageable effective = captor.getValue();
        assertThat(effective.getPageSize()).isEqualTo(50);
        assertThat(effective.getSort().getOrderFor("lastName").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    // ==================== Input Validation Tests ====================

    @Test
    void syncSingleNinjaLocal_throwsOnNullFacilityId() {
        LocalSyncRequest request = createTestRequest();

        assertThatThrownBy(() -> ninjaService.syncSingleNinjaLocal(null, "student-1", request))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("facilityId must not be null");
    }

    @Test
    void syncSingleNinjaLocal_throwsOnBlankStudentId() {
        UUID facilityId = UUID.randomUUID();
        LocalSyncRequest request = createTestRequest();

        assertThatThrownBy(() -> ninjaService.syncSingleNinjaLocal(facilityId, "", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentId must not be blank");

        assertThatThrownBy(() -> ninjaService.syncSingleNinjaLocal(facilityId, "   ", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentId must not be blank");

        assertThatThrownBy(() -> ninjaService.syncSingleNinjaLocal(facilityId, null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentId must not be blank");
    }

    @Test
    void syncSingleNinjaLocal_throwsOnNullPayload() {
        UUID facilityId = UUID.randomUUID();

        assertThatThrownBy(() -> ninjaService.syncSingleNinjaLocal(facilityId, "student-1", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("payload must not be null");
    }

    // ==================== Facility Management Tests ====================

    @Test
    void syncSingleNinjaLocal_usesExistingFacility() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-123";
        Facility existingFacility = new Facility(facilityId, "Existing Facility");
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "John",
                        "Doe",
                        "JR",
                        null,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "New Facility Name");

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(existingFacility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance(anyString(), anyString())).thenReturn(10);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.updated()).isTrue();
        verify(facilityRepository, never()).save(any());
    }

    @Test
    void syncSingleNinjaLocal_createsNewFacilityWhenMissing() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-456";
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Jane",
                        "Smith",
                        "CREATE",
                        null,
                        2,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Auto Created Facility");

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());
        when(facilityRepository.save(any(Facility.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance(anyString(), anyString())).thenReturn(5);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.updated()).isTrue();

        ArgumentCaptor<Facility> facilityCaptor = ArgumentCaptor.forClass(Facility.class);
        verify(facilityRepository).save(facilityCaptor.capture());
        assertThat(facilityCaptor.getValue().getName()).isEqualTo("Auto Created Facility");
    }

    @Test
    void syncSingleNinjaLocal_usesDefaultFacilityNameWhenBlank() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-789";
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Bob",
                        "Builder",
                        "JR",
                        null,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "  ");

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());
        when(facilityRepository.save(any(Facility.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance(anyString(), anyString())).thenReturn(0);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        ArgumentCaptor<Facility> facilityCaptor = ArgumentCaptor.forClass(Facility.class);
        verify(facilityRepository).save(facilityCaptor.capture());
        assertThat(facilityCaptor.getValue().getName())
                .startsWith("Facility ")
                .hasSize(17); // "Facility " + 8 chars from UUID
    }

    @Test
    void syncSingleNinjaLocal_handlesRaceConditionOnFacilityCreation() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-race";
        Facility racedFacility = new Facility(facilityId, "Raced Facility");
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Race",
                        "Condition",
                        "JR",
                        null,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "My Facility");

        when(facilityRepository.findById(facilityId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(racedFacility));
        when(facilityRepository.save(any(Facility.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance(anyString(), anyString())).thenReturn(0);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.updated()).isTrue();
        verify(facilityRepository, times(2)).findById(facilityId);
    }

    // ==================== Reward Logic Tests ====================

    @Test
    void syncSingleNinjaLocal_awardsStepRewardOnProgression() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-progress";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLevelSequence(1); // Same level
        existingNinja.setLastCompletedSteps(5); // Was at 5 steps
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Progress",
                        "Student",
                        "Yellow Belt",
                        null,
                        1, // Same level
                        "act-1",
                        8,
                        null,
                        null,
                        10, // Now at 10 steps (5 new steps)
                        15,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(pointCalculator.getBeltMultiplier("Yellow Belt")).thenReturn(2);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes().stepReward()).isEqualTo(10); // 5 steps * 2 multiplier
        assertThat(response.changes().stepsAwarded()).isEqualTo(5);
        verify(ledgerService)
                .createTransaction(
                        eq(facilityId), eq(studentId), eq(10), any(), anyString(), isNull());
    }

    @Test
    void syncSingleNinjaLocal_noStepRewardWhenStepsDecrease() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-regress";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLevelSequence(1);
        existingNinja.setLastCompletedSteps(10);
        Facility facility = new Facility(facilityId, "Test Facility");

        // Steps decreased within same level (edge case - shouldn't normally happen)
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Regress",
                        "Student",
                        "White Belt",
                        null,
                        1, // Same level
                        "act-1",
                        5,
                        null,
                        null,
                        5, // Steps went down to 5
                        15,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes().stepReward()).isNull();
        assertThat(response.changes().updated()).isTrue();
        verify(ledgerService, never()).createTransaction(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void syncSingleNinjaLocal_awardsStepRewardWhenLevelChanges() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-level-change";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLevelSequence(1);
        existingNinja.setLastCompletedSteps(10); // Old level had 10 steps
        Facility facility = new Facility(facilityId, "Test Facility");

        // Level changed from 1 to 2
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Level",
                        "Change",
                        "White Belt",
                        null,
                        2, // NEW level
                        "act-2",
                        9,
                        null,
                        null,
                        5, // Steps in NEW level - should award all 5
                        10,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(pointCalculator.getBeltMultiplier("White Belt")).thenReturn(1);
        when(pointCalculator.getProgressionMultiplier("White Belt")).thenReturn(10);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        // Should award for all 5 steps in new level + level progression
        assertThat(response.changes().stepReward()).isEqualTo(5);
        assertThat(response.changes().stepsAwarded()).isEqualTo(5);
        assertThat(response.changes().levelProgressionReward()).isEqualTo(10); // 1 level * 10
        // 2 transactions: step reward + level progression
        verify(ledgerService, times(2)).createTransaction(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void syncSingleNinjaLocal_awardsLevelProgressionReward() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-level";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLevelSequence(3);
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Level",
                        "Up",
                        "JR",
                        null,
                        5,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(pointCalculator.getProgressionMultiplier("JR")).thenReturn(25);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes().levelProgressionReward()).isEqualTo(50); // 2 levels * 25
        assertThat(response.changes().levelDifference()).isEqualTo(2);
        assertThat(response.changes().oldLevelSequence()).isEqualTo(3);
        assertThat(response.changes().newLevelSequence()).isEqualTo(5);
        verify(ledgerService)
                .createTransaction(
                        eq(facilityId), eq(studentId), eq(50), any(), anyString(), isNull());
    }

    @Test
    void syncSingleNinjaLocal_noLevelRewardWhenLevelDecreases() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-level-regress";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLevelSequence(5);
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Level",
                        "Down",
                        "JR",
                        null,
                        3,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes().levelProgressionReward()).isNull();
        verify(ledgerService, never()).createTransaction(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void syncSingleNinjaLocal_awardsBothStepAndLevelRewards() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-both";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLevelSequence(1);
        existingNinja.setLastCompletedSteps(5); // Old level had 5 steps
        Facility facility = new Facility(facilityId, "Test Facility");

        // Level changed from 1 to 3, new level has 10 steps completed
        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Both",
                        "Rewards",
                        "White Belt",
                        null,
                        3, // Level progression (1 -> 3)
                        "act-1",
                        10,
                        null,
                        null,
                        10, // 10 steps in NEW level (all awarded since level changed)
                        15,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(pointCalculator.getBeltMultiplier("White Belt")).thenReturn(1);
        when(pointCalculator.getProgressionMultiplier("White Belt")).thenReturn(10);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes().stepReward()).isEqualTo(10); // 10 steps in new level * 1
        assertThat(response.changes().levelProgressionReward()).isEqualTo(20); // 2 levels * 10
        assertThat(response.changes().updated()).isFalse(); // rewards were given
        verify(ledgerService, times(2)).createTransaction(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void syncSingleNinjaLocal_givesInitialBalanceForNewNinja() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "new-ninja";
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request =
                new LocalSyncRequest(
                        "New",
                        "Ninja",
                        "JR",
                        null,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance("JR", "Level 1")).thenReturn(100);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes().initialBalance()).isEqualTo(100);
        verify(ledgerService)
                .createTransaction(
                        eq(facilityId),
                        eq(studentId),
                        eq(100),
                        any(),
                        anyString(),
                        isNull(),
                        eq(LocalDateTime.of(1970, 1, 1, 0, 0)));
    }

    @Test
    void syncSingleNinjaLocal_noInitialBalanceWhenZero() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "zero-balance-ninja";
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request =
                new LocalSyncRequest(
                        "Zero",
                        "Balance",
                        "Unknown Belt",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentIdForUpdate(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance("Unknown Belt", null)).thenReturn(0);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response =
                ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes().initialBalance()).isEqualTo(0);
        verify(ledgerService, never()).createTransaction(any(), any(), anyInt(), any(), any(), any());
    }

    // ==================== Existence Check Tests ====================

    @Test
    void ninjaExists_returnsTrueWhenExists() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "existing-student";

        when(ninjaRepository.existsByFacilityIdAndStudentId(facilityId, studentId)).thenReturn(true);

        assertThat(ninjaService.ninjaExists(facilityId, studentId)).isTrue();
    }

    @Test
    void ninjaExists_returnsFalseWhenNotExists() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "non-existing-student";

        when(ninjaRepository.existsByFacilityIdAndStudentId(facilityId, studentId)).thenReturn(false);

        assertThat(ninjaService.ninjaExists(facilityId, studentId)).isFalse();
    }

    // ==================== Helper Methods ====================

    private LocalSyncRequest createTestRequest() {
        return new LocalSyncRequest(
                "Test", "User", "JR", null, 1, null, null, null, null, null, null, null, null);
    }
}
