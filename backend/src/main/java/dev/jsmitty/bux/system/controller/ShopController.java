package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.service.ShopService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Shop catalog endpoints for a facility.
 *
 * <p>Public read access; writes require facility admin access.
 */
@RestController
@RequestMapping("/api/facilities/{facilityId}/shop")
public class ShopController {

    private final ShopService shopService;
    private final FacilityAccessChecker accessChecker;

    public ShopController(ShopService shopService, FacilityAccessChecker accessChecker) {
        this.shopService = shopService;
        this.accessChecker = accessChecker;
    }

    /** List shop items for a facility (public). */
    @GetMapping
    public ResponseEntity<ShopListResponse> getShopItems(@PathVariable UUID facilityId) {
        // Public endpoint - no auth check needed
        return ResponseEntity.ok(shopService.getShopItems(facilityId));
    }

    /** Create a shop item (admin access). */
    @PostMapping
    public ResponseEntity<ShopItemResponse> createShopItem(
            @PathVariable UUID facilityId, @Valid @RequestBody ShopItemRequest request) {
        accessChecker.checkFacilityAccess(facilityId);
        return ResponseEntity.ok(shopService.createShopItem(facilityId, request));
    }

    /** Update a shop item (admin access). */
    @PutMapping("/{itemId}")
    public ResponseEntity<ShopItemResponse> updateShopItem(
            @PathVariable UUID facilityId,
            @PathVariable Long itemId,
            @Valid @RequestBody ShopItemRequest request) {
        accessChecker.checkFacilityAccess(facilityId);
        return shopService
                .updateShopItem(facilityId, itemId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Delete a shop item (admin access). */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteShopItem(
            @PathVariable UUID facilityId, @PathVariable Long itemId) {
        accessChecker.checkFacilityAccess(facilityId);
        if (shopService.deleteShopItem(facilityId, itemId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
