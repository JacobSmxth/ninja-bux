package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.security.AdminUserDetails;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Authorization helper for facility-scoped access checks.
 *
 * <p>Used by controllers to enforce admin and super-admin permissions.
 */
@Component
public class FacilityAccessChecker {

    /**
     * Resolves the current authenticated admin from the security context.
     */
    public AdminUserDetails getCurrentAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AdminUserDetails) {
            return (AdminUserDetails) auth.getPrincipal();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    public Long getCurrentAdminId() {
        return getCurrentAdmin().getAdminId();
    }

    /**
     * Ensures the current admin can access the given facility.
     */
    public void checkFacilityAccess(UUID facilityId) {
        AdminUserDetails admin = getCurrentAdmin();
        if (admin.isSuperAdmin()) {
            return;
        }
        if (!admin.getFacilityIds().contains(facilityId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Access denied to this facility");
        }
    }

    /**
     * Ensures the current admin is a super admin.
     */
    public void checkSuperAdmin() {
        AdminUserDetails admin = getCurrentAdmin();
        if (!admin.isSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin access required");
        }
    }

    public boolean isSuperAdmin() {
        try {
            AdminUserDetails admin = getCurrentAdmin();
            return admin.isSuperAdmin();
        } catch (ResponseStatusException e) {
            return false;
        }
    }
}
