package dev.jsmitty.bux.system.controller;

import dev.jsmitty.bux.system.dto.AdminListResponse;
import dev.jsmitty.bux.system.dto.AdminResponse;
import dev.jsmitty.bux.system.dto.CreateAdminRequest;
import dev.jsmitty.bux.system.dto.FacilityResponse;
import dev.jsmitty.bux.system.repository.FacilityRepository;
import dev.jsmitty.bux.system.service.AdminService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only management endpoints.
 *
 * <p>Requires a super admin for most operations. Uses {@link FacilityAccessChecker}
 * to enforce permissions.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final FacilityRepository facilityRepository;
    private final FacilityAccessChecker accessChecker;

    public AdminController(
            AdminService adminService,
            FacilityRepository facilityRepository,
            FacilityAccessChecker accessChecker) {
        this.adminService = adminService;
        this.facilityRepository = facilityRepository;
        this.accessChecker = accessChecker;
    }

    /** Lists all admins (super admin only). */
    @GetMapping("/admins")
    public ResponseEntity<AdminListResponse> getAllAdmins() {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    /** Fetch a single admin by id (super admin only). */
    @GetMapping("/admins/{id}")
    public ResponseEntity<AdminResponse> getAdmin(@PathVariable Long id) {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.ok(adminService.getAdmin(id));
    }

    /** Create a new admin account (super admin only). */
    @PostMapping("/admins")
    public ResponseEntity<AdminResponse> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(request));
    }

    /** Update an admin's profile, password, and facility access (super admin only). */
    @PutMapping("/admins/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable Long id, @Valid @RequestBody CreateAdminRequest request) {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.ok(adminService.updateAdmin(id, request));
    }

    /** Delete an admin (super admin only; cannot delete self). */
    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        accessChecker.checkSuperAdmin();
        adminService.deleteAdmin(id, accessChecker.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    /** List all facilities (super admin only). */
    @GetMapping("/facilities")
    public ResponseEntity<List<FacilityResponse>> getAllFacilities() {
        accessChecker.checkSuperAdmin();
        List<FacilityResponse> facilities =
                facilityRepository.findAll().stream().map(FacilityResponse::from).toList();
        return ResponseEntity.ok(facilities);
    }

    /** Return the current authenticated admin. */
    @GetMapping("/me")
    public ResponseEntity<AdminResponse> getCurrentAdmin() {
        Long adminId = accessChecker.getCurrentAdminId();
        return ResponseEntity.ok(adminService.getAdmin(adminId));
    }
}
