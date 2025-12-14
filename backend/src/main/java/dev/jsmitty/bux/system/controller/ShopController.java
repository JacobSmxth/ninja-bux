package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.service.ShopService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/facilities/{facilityId}/shop")
public class ShopController {

  private final ShopService shopService;
  private final FacilityAccessChecker accessChecker;

  public ShopController(ShopService shopService, FacilityAccessChecker accessChecker) {
    this.shopService = shopService;
    this.accessChecker = accessChecker;
  }

  @GetMapping
  public ResponseEntity<ShopListResponse> getShopItems(@PathVariable UUID facilityId) {
    accessChecker.checkFacilityAccess(facilityId);
    return ResponseEntity.ok(shopService.getShopItems(facilityId));
  }

  @PostMapping
  public ResponseEntity<ShopItemResponse> createShopItem(
      @PathVariable UUID facilityId, @Valid @RequestBody ShopItemRequest request) {
    accessChecker.checkFacilityAccess(facilityId);
    return ResponseEntity.ok(shopService.createShopItem(facilityId, request));
  }

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
