package com.mistry.platform.service;

import com.mistry.platform.dto.AdminRegisterRequest;
import com.mistry.platform.dto.AdminRegisterResponse;
import com.mistry.platform.entity.Admin;
import com.mistry.platform.exception.AdminRegistrationNotAuthorizedException;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.registration.secret-key}")
    private String adminRegistrationSecretKey;

    public AdminService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminRegisterResponse registerAdmin(AdminRegisterRequest request) {

        // AC: "Only authorized users can create an admin account" /
        // "Unauthorized users cannot access admin registration"
        if (!adminRegistrationSecretKey.equals(request.getAdminSecretKey())) {
            throw new AdminRegistrationNotAuthorizedException(
                    "You are not authorized to create an admin account.");
        }

        // AC: "System checks whether the admin email/phone is already registered"
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
}
