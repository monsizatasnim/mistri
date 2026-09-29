package com.mistry.platform.service;

import com.mistry.platform.dto.AdminLoginRequest;
import com.mistry.platform.dto.AdminLoginResponse;
import com.mistry.platform.dto.AdminRegisterRequest;
import com.mistry.platform.dto.AdminRegisterResponse;
import com.mistry.platform.entity.Admin;
import com.mistry.platform.exception.AdminRegistrationNotAuthorizedException;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.AdminRepository;
import com.mistry.platform.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${admin.registration.secret-key}")
    private String adminRegistrationSecretKey;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AdminRegisterResponse registerAdmin(AdminRegisterRequest request) {

        if (!adminRegistrationSecretKey.equals(request.getAdminSecretKey())) {
            throw new AdminRegistrationNotAuthorizedException(
                    "You are not authorized to create an admin account.");
        }

        if (adminRepository.existsByEmail(request.getEmail())
                || adminRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateAccountException(
                    "An admin account with this email or phone number already exists.");
        }

        Admin admin = new Admin(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword())
        );

        Admin saved = adminRepository.save(admin);

        return new AdminRegisterResponse(
                saved.getAdminId(),
                "Admin registration successful. You can now log in.");
    }

    public AdminLoginResponse loginAdmin(AdminLoginRequest request) {

        Admin admin = adminRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(admin.getAdminId(), admin.getEmail(), admin.getRole());

        return new AdminLoginResponse(token, admin.getAdminId(), admin.getFullName(), "Login successful.");
    }
}