package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.service.AdjustmentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/facilities/{facilityId}")
public class AdjustmentController {

    private final AdjustmentService adjustmentService;
    private final FacilityAccessChecker accessChecker;

    public AdjustmentController(
            AdjustmentService adjustmentService, FacilityAccessChecker accessChecker) {
        this.adjustmentService = adjustmentService;
        this.accessChecker = accessChecker;
    }

    @PostMapping("/ninjas/{studentId}/adjustments")
    public ResponseEntity<?> createAdjustment(
            @PathVariable UUID facilityId,
            @PathVariable String studentId,
            @Valid @RequestBody AdjustmentRequest request) {
        accessChecker.checkFacilityAccess(facilityId);
        try {
            Long adminId = accessChecker.getCurrentAdminId();
            AdjustmentResponse response =
                    adjustmentService.createAdjustment(facilityId, studentId, adminId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/adjustments")
    public ResponseEntity<AdjustmentListResponse> getAdjustments(
            @PathVariable UUID facilityId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        accessChecker.checkFacilityAccess(facilityId);
        return ResponseEntity.ok(adjustmentService.getAdjustments(facilityId, limit, offset));
    }
}
