package dev.jsmitty.bux.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import dev.jsmitty.bux.system.domain.LedgerTxn;
import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.LedgerResponse;
import dev.jsmitty.bux.system.repository.LedgerTxnRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock LedgerTxnRepository ledgerTxnRepository;
    @Mock NinjaRepository ninjaRepository;

    @InjectMocks LedgerService ledgerService;

    // ==================== Input Validation Tests ====================

    @Test
    void createTransaction_throwsOnNullFacilityId() {
        assertThatThrownBy(
                        () ->
                                ledgerService.createTransaction(
                                        null, "student-1", 100, TxnType.ACTIVITY_REWARD, "desc", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("facilityId must not be null");
    }

    @Test
    void createTransaction_throwsOnNullStudentId() {
        UUID facilityId = UUID.randomUUID();

        assertThatThrownBy(
                        () ->
                                ledgerService.createTransaction(
                                        facilityId, null, 100, TxnType.ACTIVITY_REWARD, "desc", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentId must not be blank");
    }

    @Test
    void createTransaction_throwsOnBlankStudentId() {
        UUID facilityId = UUID.randomUUID();

        assertThatThrownBy(
                        () ->
                                ledgerService.createTransaction(
                                        facilityId, "", 100, TxnType.ACTIVITY_REWARD, "desc", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentId must not be blank");

        assertThatThrownBy(
                        () ->
                                ledgerService.createTransaction(
                                        facilityId, "   ", 100, TxnType.ACTIVITY_REWARD, "desc", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentId must not be blank");
    }

    @Test
    void createTransaction_throwsOnNullAmount() {
        UUID facilityId = UUID.randomUUID();

        assertThatThrownBy(
                        () ->
                                ledgerService.createTransaction(
                                        facilityId, "student-1", null, TxnType.ACTIVITY_REWARD, "desc", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("amount must not be null");
    }

    @Test
    void createTransaction_throwsOnNullType() {
        UUID facilityId = UUID.randomUUID();

        assertThatThrownBy(
                        () ->
                                ledgerService.createTransaction(
                                        facilityId, "student-1", 100, null, "desc", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("type must not be null");
    }

    // ==================== Transaction Creation Tests ====================

    @Test
    void createTransaction_savesTransactionAndUpdatesNinjaBalance() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";
        Ninja ninja = new Ninja(studentId, facilityId);

        when(ledgerTxnRepository.save(any(LedgerTxn.class))).thenAnswer(inv -> {
            LedgerTxn txn = inv.getArgument(0);
            txn.setId(1L);
            return txn;
        });
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.of(ninja));
        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(150);
        when(ninjaRepository.save(any(Ninja.class))).thenAnswer(inv -> inv.getArgument(0));

        LedgerTxn result = ledgerService.createTransaction(
                facilityId, studentId, 100, TxnType.ACTIVITY_REWARD, "Test reward", null);

        assertThat(result.getAmount()).isEqualTo(100);
        assertThat(result.getType()).isEqualTo(TxnType.ACTIVITY_REWARD);

        // Verify ninja balance was updated
        ArgumentCaptor<Ninja> ninjaCaptor = ArgumentCaptor.forClass(Ninja.class);
        verify(ninjaRepository).save(ninjaCaptor.capture());
        assertThat(ninjaCaptor.getValue().getCurrentBalance()).isEqualTo(150);
    }

    @Test
    void createTransaction_handlesNinjaNotFound() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "non-existent";

        when(ledgerTxnRepository.save(any(LedgerTxn.class))).thenAnswer(inv -> {
            LedgerTxn txn = inv.getArgument(0);
            txn.setId(1L);
            return txn;
        });
        when(ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId))
                .thenReturn(Optional.empty());

        // Should not throw - just skip ninja balance update
        LedgerTxn result = ledgerService.createTransaction(
                facilityId, studentId, 100, TxnType.ACTIVITY_REWARD, "Test", null);

        assertThat(result).isNotNull();
        verify(ninjaRepository, never()).save(any());
    }

    // ==================== Pagination Tests ====================

    @Test
    void getLedger_withPageable_capsPageSize() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";

        Page<LedgerTxn> emptyPage = new PageImpl<>(List.of());
        when(ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(0);

        // Request size 500, should be capped to 100
        ledgerService.getLedger(facilityId, studentId, PageRequest.of(0, 500));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ledgerTxnRepository)
                .findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void getLedger_withPageable_appliesDefaultSize() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";

        Page<LedgerTxn> emptyPage = new PageImpl<>(List.of());
        when(ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(0);

        // Using Unpaged, should use default 50
        ledgerService.getLedger(facilityId, studentId, Pageable.unpaged());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ledgerTxnRepository)
                .findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void getLedger_alwaysSortsByCreatedAtDesc() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";

        Page<LedgerTxn> emptyPage = new PageImpl<>(List.of());
        when(ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(0);

        ledgerService.getLedger(facilityId, studentId, PageRequest.of(0, 10));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ledgerTxnRepository)
                .findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getSort().isSorted()).isTrue();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt").isDescending())
                .isTrue();
    }

    @Test
    @SuppressWarnings("deprecation")
    void getLedger_deprecatedMethod_calculatesPageCorrectly() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";

        Page<LedgerTxn> emptyPage = new PageImpl<>(List.of());
        when(ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(0);

        // offset=20, limit=10 should be page 2
        ledgerService.getLedger(facilityId, studentId, 10, 20);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ledgerTxnRepository)
                .findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @SuppressWarnings("deprecation")
    void getLedger_deprecatedMethod_capsPageSize() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";

        Page<LedgerTxn> emptyPage = new PageImpl<>(List.of());
        when(ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(0);

        // limit=500 should be capped to 100
        ledgerService.getLedger(facilityId, studentId, 500, 0);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(ledgerTxnRepository)
                .findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
    }

    // ==================== Balance Tests ====================

    @Test
    void getBalance_returnsCalculatedBalance() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";

        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(250);

        Integer balance = ledgerService.getBalance(facilityId, studentId);

        assertThat(balance).isEqualTo(250);
    }

    @Test
    void getLedger_returnsTransactionsAndBalance() {
        UUID facilityId = UUID.randomUUID();
        String studentId = "student-1";

        LedgerTxn txn1 = new LedgerTxn(facilityId, studentId, 100, TxnType.ACTIVITY_REWARD, "Reward 1");
        txn1.setId(1L);
        LedgerTxn txn2 = new LedgerTxn(facilityId, studentId, 50, TxnType.ACTIVITY_REWARD, "Reward 2");
        txn2.setId(2L);

        Page<LedgerTxn> page = new PageImpl<>(List.of(txn1, txn2));
        when(ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        eq(facilityId), eq(studentId), any(Pageable.class)))
                .thenReturn(page);
        when(ledgerTxnRepository.calculateBalance(facilityId, studentId)).thenReturn(150);

        LedgerResponse response = ledgerService.getLedger(facilityId, studentId, PageRequest.of(0, 10));

        assertThat(response.transactions()).hasSize(2);
        assertThat(response.currentBalance()).isEqualTo(150);
        assertThat(response.transactions().get(0).amount()).isEqualTo(100);
        assertThat(response.transactions().get(1).amount()).isEqualTo(50);
    }
}
