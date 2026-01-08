package dev.jsmitty.bux.system.dto;

import dev.jsmitty.bux.system.domain.ShopItem;

/**
 * Response payload describing a shop item.
 */
public record ShopItemResponse(
        Long id, String name, String description, Integer price, Boolean isAvailable) {
    public static ShopItemResponse from(ShopItem item) {
        return new ShopItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getIsAvailable());
    }
}
