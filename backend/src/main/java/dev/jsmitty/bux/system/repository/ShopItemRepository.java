package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.ShopItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
  List<ShopItem> findByFacilityId(UUID facilityId);

  List<ShopItem> findByFacilityIdAndIsAvailableTrue(UUID facilityId);

  Optional<ShopItem> findByIdAndFacilityId(Long id, UUID facilityId);
}
