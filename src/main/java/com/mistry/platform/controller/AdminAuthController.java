package com.mistry.platform.controller;

import com.mistry.platform.dto.AdminRegisterRequest;
import com.mistry.platform.dto.AdminRegisterResponse;
import com.mistry.platform.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/admin/register")
    public ResponseEntity<AdminRegisterResponse> register(@Valid @RequestBody AdminRegisterRequest request) {
        AdminRegisterResponse response = adminService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}