package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.domain.PurchaseStatus;
import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.service.PurchaseService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facilities/{facilityId}")
public class PurchaseController {

  private final PurchaseService purchaseService;
  private final FacilityAccessChecker accessChecker;

  public PurchaseController(PurchaseService purchaseService, FacilityAccessChecker accessChecker) {
    this.purchaseService = purchaseService;
    this.accessChecker = accessChecker;
  }

  @PostMapping("/ninjas/{studentId}/purchases")
  public ResponseEntity<?> makePurchase(
      @PathVariable UUID facilityId,
      @PathVariable String studentId,
      @Valid @RequestBody PurchaseRequest request) {
    // Public endpoint - no auth check needed (ninjas can purchase)
    try {
      PurchaseResponse response = purchaseService.makePurchase(facilityId, studentId, request);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/purchases")
  public ResponseEntity<PurchaseListResponse> getPurchases(
      @PathVariable UUID facilityId,
      @RequestParam(required = false) PurchaseStatus status,
      @RequestParam(defaultValue = "50") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    accessChecker.checkFacilityAccess(facilityId);
    return ResponseEntity.ok(purchaseService.getPurchases(facilityId, status, limit, offset));
  }

  @PutMapping("/purchases/{purchaseId}/fulfill")
  public ResponseEntity<PurchaseResponse> fulfillPurchase(
      @PathVariable UUID facilityId, @PathVariable Long purchaseId) {
    accessChecker.checkFacilityAccess(facilityId);
    return purchaseService
        .fulfillPurchase(facilityId, purchaseId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/purchases/{purchaseId}/cancel")
  public ResponseEntity<PurchaseResponse> cancelPurchase(
      @PathVariable UUID facilityId, @PathVariable Long purchaseId) {
    accessChecker.checkFacilityAccess(facilityId);
    return purchaseService
        .cancelPurchase(facilityId, purchaseId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
