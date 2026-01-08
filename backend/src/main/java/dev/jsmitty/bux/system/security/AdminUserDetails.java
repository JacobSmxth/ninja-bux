package dev.jsmitty.bux.system.security;

import dev.jsmitty.bux.system.domain.Admin;
import dev.jsmitty.bux.system.domain.Facility;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UserDetails wrapper around {@link dev.jsmitty.bux.system.domain.Admin}.
 *
 * <p>Exposes roles and facility access for authorization checks.
 */
public class AdminUserDetails implements UserDetails {

    private final Admin admin;

    public AdminUserDetails(Admin admin) {
        this.admin = admin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (admin.isSuperAdmin()) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public String getPassword() {
        return admin.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return admin.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getAdminId() {
        return admin.getId();
    }

    public Set<UUID> getFacilityIds() {
        return admin.getFacilities().stream().map(Facility::getId).collect(Collectors.toSet());
    }

    public Admin getAdmin() {
        return admin;
    }

    public boolean isSuperAdmin() {
        return admin.isSuperAdmin();
    }
}
