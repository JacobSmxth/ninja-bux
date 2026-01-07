package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.LedgerTxn;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.LedgerResponse;
import dev.jsmitty.bux.system.dto.LedgerTxnResponse;
import dev.jsmitty.bux.system.repository.LedgerTxnRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LedgerService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final LedgerTxnRepository ledgerTxnRepository;
    private final NinjaRepository ninjaRepository;

    public LedgerService(LedgerTxnRepository ledgerTxnRepository, NinjaRepository ninjaRepository) {
        this.ledgerTxnRepository = ledgerTxnRepository;
        this.ninjaRepository = ninjaRepository;
    }

    // ==================== Write Operations ====================

    @Transactional
    public LedgerTxn createTransaction(
            UUID facilityId,
            String studentId,
            Integer amount,
            TxnType type,
            String description,
            Long relatedEntityId) {
        return createTransaction(
                facilityId, studentId, amount, type, description, relatedEntityId, null);
    }

    @Transactional
    public LedgerTxn createTransaction(
            UUID facilityId,
            String studentId,
            Integer amount,
            TxnType type,
            String description,
            Long relatedEntityId,
            LocalDateTime createdAt) {
        validateTransactionInput(facilityId, studentId, amount, type);

        LedgerTxn txn = new LedgerTxn(facilityId, studentId, amount, type, description);
        txn.setRelatedEntityId(relatedEntityId);
        if (createdAt != null) {
            txn.setCreatedAt(createdAt);
        }
        LedgerTxn saved = ledgerTxnRepository.save(txn);

        updateNinjaBalance(facilityId, studentId);

        return saved;
    }

    private void validateTransactionInput(
            UUID facilityId, String studentId, Integer amount, TxnType type) {
        Objects.requireNonNull(facilityId, "facilityId must not be null");
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId must not be blank");
        }
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(type, "type must not be null");
    }

    private void updateNinjaBalance(UUID facilityId, String studentId) {
        ninjaRepository
                .findByFacilityIdAndStudentId(facilityId, studentId)
                .ifPresent(
                        ninja -> {
                            Integer balance =
                                    ledgerTxnRepository.calculateBalance(facilityId, studentId);
                            ninja.setCurrentBalance(balance);
                            ninjaRepository.save(ninja);
                        });
    }

    // ==================== Read Operations ====================

    /**
     * Gets paginated ledger transactions for a ninja. Uses readOnly transaction to ensure
     * consistent reads between fetching transactions and calculating balance.
     */
    @Transactional(readOnly = true)
    public LedgerResponse getLedger(UUID facilityId, String studentId, Pageable pageable) {
        Pageable effective = withDefaultsAndLimits(pageable);

        Page<LedgerTxn> page =
                ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        facilityId, studentId, effective);

        List<LedgerTxnResponse> transactions =
                page.getContent().stream().map(LedgerTxnResponse::from).toList();

        Integer balance = ledgerTxnRepository.calculateBalance(facilityId, studentId);

        return new LedgerResponse(transactions, balance);
    }

    /**
     * @deprecated Use {@link #getLedger(UUID, String, Pageable)} instead
     */
    @Deprecated
    @Transactional(readOnly = true)
    public LedgerResponse getLedger(UUID facilityId, String studentId, int limit, int offset) {
        // Convert offset/limit to page number (for backwards compatibility)
        int pageSize = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
        int pageNumber = pageSize > 0 ? offset / pageSize : 0;

        return getLedger(facilityId, studentId, PageRequest.of(pageNumber, pageSize));
    }

    private Pageable withDefaultsAndLimits(Pageable pageable) {
        int requestedSize = pageable.isPaged() ? pageable.getPageSize() : 0;
        int size;
        if (requestedSize <= 0) {
            size = DEFAULT_PAGE_SIZE;
        } else {
            size = Math.min(requestedSize, MAX_PAGE_SIZE);
        }

        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;

        // Always sort by createdAt DESC for ledger (most recent first)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        return PageRequest.of(pageNumber, size, sort);
    }

    @Transactional(readOnly = true)
    public Integer getBalance(UUID facilityId, String studentId) {
        return ledgerTxnRepository.calculateBalance(facilityId, studentId);
    }
}
