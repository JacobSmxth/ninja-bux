package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.security.AdminUserDetails;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FacilityAccessChecker {

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

  public void checkFacilityAccess(UUID facilityId) {
    AdminUserDetails admin = getCurrentAdmin();
    if (admin.isSuperAdmin()) {
      return;
    }
    if (!admin.getFacilityIds().contains(facilityId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this facility");
    }
  }

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
