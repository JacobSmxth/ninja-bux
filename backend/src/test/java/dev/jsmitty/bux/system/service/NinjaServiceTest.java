package dev.jsmitty.bux.system.service;

import static org.assertj.core.api.Assertions.assertThat;
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
    void syncSingleNinjaLocal_useExistingFacility() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-123";
        Facility existingFacility = new Facility(facilityId, "Existing Facility");
        LocalSyncRequest request = new LocalSyncRequest(
                "John", "Doe", "JR", null, 1, null, null,
                null, null, null, null, null, "New Facility Name");

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(existingFacility));
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance(anyString(), anyString())).thenReturn(10);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response = ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.updated()).isTrue();
        verify(facilityRepository, never()).save(any());
    }

    @Test
    void syncSingleNinjaLocal_createsNewFacilityWhenMissing() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-456";
        LocalSyncRequest request = new LocalSyncRequest(
                "Jane", "Smith", "CREATE", null, 2, null, null,
                null, null, null, null, null, "Auto Created Facility");

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());
        when(facilityRepository.save(any(Facility.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance(anyString(), anyString())).thenReturn(5);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response = ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

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
        LocalSyncRequest request = new LocalSyncRequest(
                "Bob", "Builder", "JR", null, 1, null, null,
                null, null, null, null, null, "  ");

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());
        when(facilityRepository.save(any(Facility.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
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
        LocalSyncRequest request = new LocalSyncRequest(
                "Race", "Condition", "JR", null, 1, null, null,
                null, null, null, null, null, "My Facility");

        when(facilityRepository.findById(facilityId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(racedFacility));
        when(facilityRepository.save(any(Facility.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance(anyString(), anyString())).thenReturn(0);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response = ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.studentId()).isEqualTo(studentId);
        assertThat(response.updated()).isTrue();
        verify(facilityRepository, times(2)).findById(facilityId);
    }

    @Test
    void syncSingleNinjaLocal_awardsActivityRewardOnProgression() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-progress";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLastActivitySequence(5);
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request = new LocalSyncRequest(
                "Progress", "Student", "CREATE", null, 1, "act-1", 8,
                null, null, null, null, null, null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(pointCalculator.getBeltMultiplier("CREATE")).thenReturn(10);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response = ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes()).containsEntry("activityReward", 30); // 3 activities * 10
        assertThat(response.changes()).containsEntry("activitiesCompleted", 3);
        verify(ledgerService).createTransaction(eq(facilityId), eq(studentId), eq(30), any(), anyString(), isNull());
    }

    @Test
    void syncSingleNinjaLocal_awardsLevelProgressionReward() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-level";
        Ninja existingNinja = new Ninja(studentId, facilityId);
        existingNinja.setLevelSequence(3);
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request = new LocalSyncRequest(
                "Level", "Up", "JR", null, 5, null, null,
                null, null, null, null, null, null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.of(existingNinja));
        when(pointCalculator.getProgressionMultiplier("JR")).thenReturn(25);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response = ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes()).containsEntry("levelProgressionReward", 50); // 2 levels * 25
        assertThat(response.changes()).containsEntry("levelDifference", 2);
        verify(ledgerService).createTransaction(eq(facilityId), eq(studentId), eq(50), any(), anyString(), isNull());
    }

    @Test
    void syncSingleNinjaLocal_givesInitialBalanceForNewNinja() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "new-ninja";
        Facility facility = new Facility(facilityId, "Test Facility");

        LocalSyncRequest request = new LocalSyncRequest(
                "New", "Ninja", "JR", null, 1, null, null,
                null, null, null, null, null, null);

        when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility));
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.empty());
        when(pointCalculator.calculateInitialBalance("JR", "Level 1")).thenReturn(100);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        SingleSyncResponse response = ninjaService.syncSingleNinjaLocal(facilityId, studentId, request);

        assertThat(response.changes()).containsEntry("initialBalance", 100);
        verify(ledgerService).createTransaction(eq(facilityId), eq(studentId), eq(100), any(), anyString(), isNull());
    }
}
