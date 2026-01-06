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

    @GetMapping("/admins")
    public ResponseEntity<AdminListResponse> getAllAdmins() {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/admins/{id}")
    public ResponseEntity<AdminResponse> getAdmin(@PathVariable Long id) {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.ok(adminService.getAdmin(id));
    }

    @PostMapping("/admins")
    public ResponseEntity<AdminResponse> createAdmin(
            @Valid @RequestBody CreateAdminRequest request) {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(request));
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable Long id, @Valid @RequestBody CreateAdminRequest request) {
        accessChecker.checkSuperAdmin();
        return ResponseEntity.ok(adminService.updateAdmin(id, request));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        accessChecker.checkSuperAdmin();
        adminService.deleteAdmin(id, accessChecker.getCurrentAdminId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/facilities")
    public ResponseEntity<List<FacilityResponse>> getAllFacilities() {
        accessChecker.checkSuperAdmin();
        List<FacilityResponse> facilities =
                facilityRepository.findAll().stream().map(FacilityResponse::from).toList();
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/me")
    public ResponseEntity<AdminResponse> getCurrentAdmin() {
        Long adminId = accessChecker.getCurrentAdminId();
        return ResponseEntity.ok(adminService.getAdmin(adminId));
    }
}
