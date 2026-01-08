package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Facility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Data access for {@link dev.jsmitty.bux.system.domain.Facility}.
 *
 * <p>Used by admin management and sync flows to resolve or auto-create facilities.
 */
@Repository
public interface FacilityRepository extends JpaRepository<Facility, UUID> {}
