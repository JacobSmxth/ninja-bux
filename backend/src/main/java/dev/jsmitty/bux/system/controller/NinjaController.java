package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.dto.LocalSyncRequest;
import dev.jsmitty.bux.system.service.LedgerService;
import dev.jsmitty.bux.system.service.NinjaService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/facilities/{facilityId}/ninjas")
public class NinjaController {

    private final NinjaService ninjaService;
    private final LedgerService ledgerService;
    private final FacilityAccessChecker accessChecker;

    public NinjaController(
            NinjaService ninjaService,
            LedgerService ledgerService,
            FacilityAccessChecker accessChecker) {
        this.ninjaService = ninjaService;
        this.ledgerService = ledgerService;
        this.accessChecker = accessChecker;
    }

    @GetMapping
    public ResponseEntity<NinjaListResponse> getNinjas(
            @PathVariable UUID facilityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        // Public endpoint - no auth check needed
        return ResponseEntity.ok(ninjaService.getNinjas(facilityId, page, size));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<NinjaResponse> getNinja(
            @PathVariable UUID facilityId, @PathVariable String studentId) {
        // Public endpoint - no auth check needed
        return ninjaService
                .getNinja(facilityId, studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync")
    public ResponseEntity<SyncResponse> syncAllNinjas(@PathVariable UUID facilityId) {
        accessChecker.checkFacilityAccess(facilityId);
        return ResponseEntity.ok(ninjaService.syncAllNinjas(facilityId));
    }

    @PostMapping("/{studentId}/sync")
    public ResponseEntity<SingleSyncResponse> syncSingleNinja(
            @PathVariable UUID facilityId,
            @PathVariable String studentId,
            @Valid @RequestBody CodeNinjasLoginRequest loginRequest) {
        return ResponseEntity.ok(ninjaService.syncSingleNinja(facilityId, studentId, loginRequest));
    }

    @PostMapping("/{studentId}/sync-local")
    public ResponseEntity<SingleSyncResponse> syncSingleNinjaLocal(
            @PathVariable UUID facilityId,
            @PathVariable String studentId,
            @Valid @RequestBody LocalSyncRequest request) {
        return ResponseEntity.ok(ninjaService.syncSingleNinjaLocal(facilityId, studentId, request));
    }

    @GetMapping("/{studentId}/ledger")
    public ResponseEntity<LedgerResponse> getLedger(
            @PathVariable UUID facilityId,
            @PathVariable String studentId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        // Public endpoint - no auth check needed
        return ResponseEntity.ok(ledgerService.getLedger(facilityId, studentId, limit, offset));
    }
}
