package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.Adjustment;
import dev.jsmitty.bux.system.domain.LedgerTxn;
import dev.jsmitty.bux.system.domain.Ninja;
import dev.jsmitty.bux.system.domain.TxnType;
import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.repository.AdjustmentRepository;
import dev.jsmitty.bux.system.repository.AdminRepository;
import dev.jsmitty.bux.system.repository.NinjaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdjustmentService {

    private final AdjustmentRepository adjustmentRepository;
    private final NinjaRepository ninjaRepository;
    private final AdminRepository adminRepository;
    private final LedgerService ledgerService;

    public AdjustmentService(
            AdjustmentRepository adjustmentRepository,
            NinjaRepository ninjaRepository,
            AdminRepository adminRepository,
            LedgerService ledgerService) {
        this.adjustmentRepository = adjustmentRepository;
        this.ninjaRepository = ninjaRepository;
        this.adminRepository = adminRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public AdjustmentResponse createAdjustment(
            UUID facilityId, String studentId, Long adminId, AdjustmentRequest request) {
        if (!ninjaRepository.findByFacilityIdAndStudentId(facilityId, studentId).isPresent()) {
            throw new IllegalArgumentException("Ninja not found");
        }

        Adjustment adjustment =
                new Adjustment(facilityId, studentId, adminId, request.amount(), request.reason());
        Adjustment saved = adjustmentRepository.save(adjustment);

        LedgerTxn txn =
                ledgerService.createTransaction(
                        facilityId,
                        studentId,
                        request.amount(),
                        TxnType.ADJUSTMENT,
                        "Adjustment: " + request.reason(),
                        saved.getId());

        Integer newBalance = ledgerService.getBalance(facilityId, studentId);
        return AdjustmentResponse.created(saved.getId(), newBalance, txn.getId());
    }

    public AdjustmentListResponse getAdjustments(UUID facilityId, int limit, int offset) {
        Page<Adjustment> page =
                adjustmentRepository.findByFacilityIdOrderByCreatedAtDesc(
                        facilityId, PageRequest.of(offset / limit, limit));

        List<AdjustmentListItem> adjustments =
                page.getContent().stream()
                        .map(
                                adj -> {
                                    String ninjaName =
                                            ninjaRepository
                                                    .findByFacilityIdAndStudentId(
                                                            facilityId, adj.getStudentId())
                                                    .map(Ninja::getFullName)
                                                    .orElse(null);
                                    String adminUsername =
                                            adminRepository
                                                    .findById(adj.getAdminId())
                                                    .map(a -> a.getUsername())
                                                    .orElse(null);
                                    return new AdjustmentListItem(
                                            adj.getId(),
                                            adj.getStudentId(),
                                            ninjaName,
                                            adminUsername,
                                            adj.getAmount(),
                                            adj.getReason(),
                                            adj.getCreatedAt());
                                })
                        .toList();

        return new AdjustmentListResponse(adjustments);
    }
}
