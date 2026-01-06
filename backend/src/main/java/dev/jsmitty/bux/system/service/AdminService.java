package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.Admin;
import dev.jsmitty.bux.system.domain.Facility;
import dev.jsmitty.bux.system.dto.AdminListResponse;
import dev.jsmitty.bux.system.dto.AdminResponse;
import dev.jsmitty.bux.system.dto.CreateAdminRequest;
import dev.jsmitty.bux.system.repository.AdminRepository;
import dev.jsmitty.bux.system.repository.FacilityRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final FacilityRepository facilityRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            AdminRepository adminRepository,
            FacilityRepository facilityRepository,
            PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.facilityRepository = facilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminListResponse getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        List<AdminResponse> responses = admins.stream().map(AdminResponse::from).toList();
        return new AdminListResponse(responses, admins.size());
    }

    public AdminResponse getAdmin(Long id) {
        Admin admin =
                adminRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Admin not found"));
        return AdminResponse.from(admin);
    }

    @Transactional
    public AdminResponse createAdmin(CreateAdminRequest request) {
        if (adminRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        Admin admin =
                new Admin(
                        request.username(),
                        passwordEncoder.encode(request.password()),
                        request.email());
        admin.setSuperAdmin(request.superAdmin());

        if (request.facilityIds() != null && !request.facilityIds().isEmpty()) {
            Set<Facility> facilities = new HashSet<>();
            for (UUID facilityId : request.facilityIds()) {
                Facility facility =
                        facilityRepository
                                .findById(facilityId)
                                .orElseThrow(
                                        () ->
                                                new ResponseStatusException(
                                                        HttpStatus.BAD_REQUEST,
                                                        "Facility not found: " + facilityId));
                facilities.add(facility);
            }
            admin.setFacilities(facilities);
        }

        Admin saved = adminRepository.save(admin);
        return AdminResponse.from(saved);
    }

    @Transactional
    public AdminResponse updateAdmin(Long id, CreateAdminRequest request) {
        Admin admin =
                adminRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Admin not found"));

        // Check if username changed and is taken
        if (!admin.getUsername().equals(request.username())
                && adminRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        admin.setUsername(request.username());
        admin.setEmail(request.email());
        admin.setSuperAdmin(request.superAdmin());

        // Only update password if provided
        if (request.password() != null && !request.password().isBlank()) {
            admin.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.facilityIds() != null) {
            Set<Facility> facilities = new HashSet<>();
            for (UUID facilityId : request.facilityIds()) {
                Facility facility =
                        facilityRepository
                                .findById(facilityId)
                                .orElseThrow(
                                        () ->
                                                new ResponseStatusException(
                                                        HttpStatus.BAD_REQUEST,
                                                        "Facility not found: " + facilityId));
                facilities.add(facility);
            }
            admin.setFacilities(facilities);
        }

        Admin saved = adminRepository.save(admin);
        return AdminResponse.from(saved);
    }

    @Transactional
    public void deleteAdmin(Long id, Long currentAdminId) {
        if (id.equals(currentAdminId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete yourself");
        }

        Admin admin =
                adminRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Admin not found"));

        adminRepository.delete(admin);
    }
}
