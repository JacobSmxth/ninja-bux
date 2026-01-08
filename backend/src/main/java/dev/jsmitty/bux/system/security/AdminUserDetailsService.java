package dev.jsmitty.bux.system.security;

import dev.jsmitty.bux.system.domain.Admin;
import dev.jsmitty.bux.system.repository.AdminRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges {@link dev.jsmitty.bux.system.domain.Admin} to Spring Security's UserDetails.
 *
 * <p>Used by {@link dev.jsmitty.bux.system.security.JwtAuthenticationFilter} to resolve users.
 */
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
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Admin not found: " + username));
        return new AdminUserDetails(admin);
    }
}
