package dev.jsmitty.bux.system.security;

import dev.jsmitty.bux.system.domain.Admin;
import dev.jsmitty.bux.system.repository.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

  private final AdminRepository adminRepository;

  public AdminUserDetailsService(AdminRepository adminRepository) {
    this.adminRepository = adminRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Admin admin =
        adminRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));
    return new AdminUserDetails(admin);
  }
}
