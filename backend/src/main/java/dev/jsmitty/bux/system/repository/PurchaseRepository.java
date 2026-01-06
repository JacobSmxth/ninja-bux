package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Purchase;
import dev.jsmitty.bux.system.domain.PurchaseStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByFacilityId(UUID facilityId);

    Page<Purchase> findByFacilityId(UUID facilityId, Pageable pageable);

    Page<Purchase> findByFacilityIdAndStatus(
            UUID facilityId, PurchaseStatus status, Pageable pageable);

    List<Purchase> findByFacilityIdAndStudentId(UUID facilityId, String studentId);

    Optional<Purchase> findByIdAndFacilityId(Long id, UUID facilityId);
}
