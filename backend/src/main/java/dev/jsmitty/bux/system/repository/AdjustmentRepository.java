package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Adjustment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdjustmentRepository extends JpaRepository<Adjustment, Long> {
    Page<Adjustment> findByFacilityIdOrderByCreatedAtDesc(UUID facilityId, Pageable pageable);

    Page<Adjustment> findByFacilityIdAndStudentIdOrderByCreatedAtDesc(
            UUID facilityId, String studentId, Pageable pageable);
}
