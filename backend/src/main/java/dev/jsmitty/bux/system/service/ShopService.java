package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.ShopItem;
import dev.jsmitty.bux.system.dto.ShopItemRequest;
import dev.jsmitty.bux.system.dto.ShopItemResponse;
import dev.jsmitty.bux.system.dto.ShopListResponse;
import dev.jsmitty.bux.system.repository.ShopItemRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {

  private final ShopItemRepository shopItemRepository;

  public ShopService(ShopItemRepository shopItemRepository) {
    this.shopItemRepository = shopItemRepository;
  }

  public ShopListResponse getShopItems(UUID facilityId) {
    List<ShopItemResponse> items =
        shopItemRepository.findByFacilityId(facilityId).stream()
            .map(ShopItemResponse::from)
            .toList();
    return new ShopListResponse(items);
  }

  public ShopListResponse getAvailableShopItems(UUID facilityId) {
    List<ShopItemResponse> items =
        shopItemRepository.findByFacilityIdAndIsAvailableTrue(facilityId).stream()
            .map(ShopItemResponse::from)
            .toList();
    return new ShopListResponse(items);
  }

  public Optional<ShopItem> getShopItem(UUID facilityId, Long itemId) {
    return shopItemRepository.findByIdAndFacilityId(itemId, facilityId);
  }

  @Transactional
  public ShopItemResponse createShopItem(UUID facilityId, ShopItemRequest request) {
    ShopItem item =
        new ShopItem(facilityId, request.name(), request.description(), request.price());
    if (request.isAvailable() != null) {
      item.setIsAvailable(request.isAvailable());
    }
    ShopItem saved = shopItemRepository.save(item);
    return ShopItemResponse.from(saved);
  }

  @Transactional
  public Optional<ShopItemResponse> updateShopItem(
      UUID facilityId, Long itemId, ShopItemRequest request) {
    return shopItemRepository
        .findByIdAndFacilityId(itemId, facilityId)
        .map(
            item -> {
              item.setName(request.name());
              item.setDescription(request.description());
              item.setPrice(request.price());
              if (request.isAvailable() != null) {
                item.setIsAvailable(request.isAvailable());
              }
              return ShopItemResponse.from(shopItemRepository.save(item));
            });
  }

  @Transactional
  public boolean deleteShopItem(UUID facilityId, Long itemId) {
    return shopItemRepository
        .findByIdAndFacilityId(itemId, facilityId)
        .map(
            item -> {
              shopItemRepository.delete(item);
              return true;
            })
        .orElse(false);
  }
}
