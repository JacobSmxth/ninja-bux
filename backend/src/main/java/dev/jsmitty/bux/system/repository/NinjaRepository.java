package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Ninja;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NinjaRepository extends JpaRepository<Ninja, Long> {
    Page<Ninja> findByFacilityId(UUID facilityId, Pageable effective);

    List<Ninja> findByFacilityId(UUID facilityId);

    Optional<Ninja> findByFacilityIdAndStudentId(UUID facilityId, String studentId);

    /**
     * Fetches the ninja with a row-level lock (SELECT ... FOR UPDATE).
     * Use this when computing and awarding rewards to prevent double-awarding
     * under concurrent sync requests.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM Ninja n WHERE n.facilityId = :facilityId AND n.studentId = :studentId")
    Optional<Ninja> findByFacilityIdAndStudentIdForUpdate(
            @Param("facilityId") UUID facilityId, @Param("studentId") String studentId);

    boolean existsByFacilityIdAndStudentId(UUID facilityId, String studentId);

    long countByFacilityId(UUID facilityId);
}
