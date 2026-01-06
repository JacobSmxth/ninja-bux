package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Ninja;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NinjaRepository extends JpaRepository<Ninja, Long> {
    Page<Ninja> findByFacilityId(UUID facilityId, Pageable pageable);

    List<Ninja> findByFacilityId(UUID facilityId);

    Optional<Ninja> findByFacilityIdAndStudentId(UUID facilityId, String studentId);

    long countByFacilityId(UUID facilityId);
}
