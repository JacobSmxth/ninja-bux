package dev.jsmitty.bux.system.service;

import dev.jsmitty.bux.system.domain.Admin;
import dev.jsmitty.bux.system.dto.FacilityResponse;
import dev.jsmitty.bux.system.dto.LoginRequest;
import dev.jsmitty.bux.system.dto.LoginResponse;
import dev.jsmitty.bux.system.repository.AdminRepository;
import dev.jsmitty.bux.system.repository.FacilityRepository;
import dev.jsmitty.bux.system.security.JwtUtil;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AdminRepository adminRepository;
    private final FacilityRepository facilityRepository;
    private final JwtUtil jwtUtil;

    public AuthService(
            AuthenticationManager authenticationManager,
            AdminRepository adminRepository,
            FacilityRepository facilityRepository,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.adminRepository = adminRepository;
        this.facilityRepository = facilityRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        Authentication auth =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(), request.password()));

        Admin admin =
                adminRepository
                        .findByUsername(request.username())
                        .orElseThrow(() -> new RuntimeException("Admin not found"));

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername());

        List<FacilityResponse> facilities =
                (admin.isSuperAdmin() ? facilityRepository.findAll() : admin.getFacilities())
                        .stream().map(FacilityResponse::from).toList();

        return new LoginResponse(
                token, admin.getId(), admin.getUsername(), admin.isSuperAdmin(), facilities);
    }
}
