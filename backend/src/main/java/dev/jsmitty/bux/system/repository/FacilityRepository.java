package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Facility;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, UUID> {}
