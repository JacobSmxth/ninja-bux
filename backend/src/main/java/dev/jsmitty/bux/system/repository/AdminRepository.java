package dev.jsmitty.bux.system.repository;

import dev.jsmitty.bux.system.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
  Optional<Admin> findByUsername(String username);

  boolean existsByUsername(String username);
}
