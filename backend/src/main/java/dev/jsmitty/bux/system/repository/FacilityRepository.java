package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Facility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, UUID> {}
