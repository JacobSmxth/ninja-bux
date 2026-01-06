package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.LedgerTxn;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.LedgerResponse;
import dev.jsmitty.bux.system.dto.LedgerTxnResponse;
import dev.jsmitty.bux.system.repository.LedgerTxnRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerTxnRepository ledgerTxnRepository;
    private final NinjaRepository ninjaRepository;

    public LedgerService(LedgerTxnRepository ledgerTxnRepository, NinjaRepository ninjaRepository) {
        this.ledgerTxnRepository = ledgerTxnRepository;
        this.ninjaRepository = ninjaRepository;
    }

    @Transactional
    public LedgerTxn createTransaction(
            UUID facilityId,
            String studentId,
            Integer amount,
            TxnType type,
            String description,
            Long relatedEntityId) {
        LedgerTxn txn = new LedgerTxn(facilityId, studentId, amount, type, description);
        txn.setRelatedEntityId(relatedEntityId);
        LedgerTxn saved = ledgerTxnRepository.save(txn);

        updateNinjaBalance(facilityId, studentId);

        return saved;
    }

    public LedgerResponse getLedger(UUID facilityId, String studentId, int limit, int offset) {
        Page<LedgerTxn> page =
                ledgerTxnRepository.findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
                        facilityId, studentId, PageRequest.of(offset / limit, limit));

        List<LedgerTxnResponse> transactions =
                page.getContent().stream().map(LedgerTxnResponse::from).toList();

        Integer balance = ledgerTxnRepository.calculateBalance(facilityId, studentId);

        return new LedgerResponse(transactions, balance);
    }

    public Integer getBalance(UUID facilityId, String studentId) {
        return ledgerTxnRepository.calculateBalance(facilityId, studentId);
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
}
