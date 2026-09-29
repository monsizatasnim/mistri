package com.mistry.platform.controller;

import com.mistry.platform.dto.AdminLoginRequest;
import com.mistry.platform.dto.AdminLoginResponse;
import com.mistry.platform.dto.AdminRegisterRequest;
import com.mistry.platform.dto.AdminRegisterResponse;
import com.mistry.platform.security.TokenBlacklistService;
import com.mistry.platform.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {

    private final AdminService adminService;
    private final TokenBlacklistService tokenBlacklistService;

    public AdminAuthController(AdminService adminService, TokenBlacklistService tokenBlacklistService) {
        this.adminService = adminService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/admin/register")
    public ResponseEntity<AdminRegisterResponse> register(@Valid @RequestBody AdminRegisterRequest request) {
        AdminRegisterResponse response = adminService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminService.loginAdmin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authHeader.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }
}