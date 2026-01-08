package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access for {@link dev.jsmitty.bux.system.domain.Admin}.
 *
 * <p>Used by {@link dev.jsmitty.bux.system.service.AuthService} for login and
 * {@link dev.jsmitty.bux.system.service.AdminService} for CRUD.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);
}
