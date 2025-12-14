package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.*;
import dev.jsmitty.bux.system.dto.*;
import dev.jsmitty.bux.system.repository.NinjaRepository;
import dev.jsmitty.bux.system.repository.PurchaseRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

  private final PurchaseRepository purchaseRepository;
  private final NinjaRepository ninjaRepository;
  private final ShopService shopService;
  private final LedgerService ledgerService;

  public PurchaseService(
      PurchaseRepository purchaseRepository,
      NinjaRepository ninjaRepository,
      ShopService shopService,
      LedgerService ledgerService) {
    this.purchaseRepository = purchaseRepository;
    this.ninjaRepository = ninjaRepository;
    this.shopService = shopService;
    this.ledgerService = ledgerService;
  }

  @Transactional
  public PurchaseResponse makePurchase(UUID facilityId, String studentId, PurchaseRequest request) {
    ShopItem item =
        shopService
            .getShopItem(facilityId, request.shopItemId())
            .orElseThrow(() -> new IllegalArgumentException("Shop item not found"));

    if (!item.getIsAvailable()) {
      throw new IllegalArgumentException("Shop item is not available");
    }

    Ninja ninja =
        ninjaRepository
            .findByFacilityIdAndStudentId(facilityId, studentId)
            .orElseThrow(() -> new IllegalArgumentException("Ninja not found"));

    if (ninja.getCurrentBalance() < item.getPrice()) {
      throw new IllegalArgumentException("Insufficient balance");
    }

    Purchase purchase =
        new Purchase(facilityId, studentId, item.getId(), item.getName(), item.getPrice());
    Purchase saved = purchaseRepository.save(purchase);

    ledgerService.createTransaction(
        facilityId,
        studentId,
        -item.getPrice(),
        TxnType.PURCHASE,
        "Purchased: " + item.getName(),
        saved.getId());

    Integer newBalance = ledgerService.getBalance(facilityId, studentId);
    return PurchaseResponse.created(saved, newBalance);
  }

  public PurchaseListResponse getPurchases(
      UUID facilityId, PurchaseStatus status, int limit, int offset) {
    Page<Purchase> page;
    if (status != null) {
      page =
          purchaseRepository.findByFacilityIdAndStatus(
              facilityId, status, PageRequest.of(offset / limit, limit));
    } else {
      page = purchaseRepository.findByFacilityId(facilityId, PageRequest.of(offset / limit, limit));
    }

    List<PurchaseListItem> purchases =
        page.getContent().stream()
            .map(
                p -> {
                  String ninjaName =
                      ninjaRepository
                          .findByFacilityIdAndStudentId(facilityId, p.getStudentId())
                          .map(Ninja::getFullName)
                          .orElse(null);
                  return new PurchaseListItem(
                      p.getId(),
                      p.getStudentId(),
                      ninjaName,
                      p.getItemName(),
                      p.getPrice(),
                      p.getStatus(),
                      p.getPurchasedAt());
                })
            .toList();

    return new PurchaseListResponse(purchases);
  }

  @Transactional
  public Optional<PurchaseResponse> fulfillPurchase(UUID facilityId, Long purchaseId) {
    return purchaseRepository
        .findByIdAndFacilityId(purchaseId, facilityId)
        .filter(p -> p.getStatus() == PurchaseStatus.PENDING)
        .map(
            purchase -> {
              purchase.setStatus(PurchaseStatus.FULFILLED);
              purchase.setFulfilledAt(LocalDateTime.now());
              Purchase saved = purchaseRepository.save(purchase);
              Integer balance = ledgerService.getBalance(facilityId, saved.getStudentId());
              return PurchaseResponse.statusUpdate(saved, false, balance);
            });
  }

  @Transactional
  public Optional<PurchaseResponse> cancelPurchase(UUID facilityId, Long purchaseId) {
    return purchaseRepository
        .findByIdAndFacilityId(purchaseId, facilityId)
        .filter(p -> p.getStatus() == PurchaseStatus.PENDING)
        .map(
            purchase -> {
              purchase.setStatus(PurchaseStatus.CANCELLED);
              Purchase saved = purchaseRepository.save(purchase);

              ledgerService.createTransaction(
                  facilityId,
                  purchase.getStudentId(),
                  purchase.getPrice(),
                  TxnType.ADJUSTMENT,
                  "Refund: Cancelled purchase of " + purchase.getItemName(),
                  saved.getId());

              Integer newBalance = ledgerService.getBalance(facilityId, purchase.getStudentId());
              return PurchaseResponse.statusUpdate(saved, true, newBalance);
            });
  }
}
