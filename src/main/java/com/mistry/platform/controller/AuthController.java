package com.mistry.platform.controller;

import com.mistry.platform.dto.ForgotPasswordRequest;
import com.mistry.platform.dto.ForgotPasswordResponse;
import com.mistry.platform.dto.LoginRequest;
import com.mistry.platform.dto.LoginResponse;
import com.mistry.platform.dto.RegisterRequest;
import com.mistry.platform.dto.RegisterResponse;
import com.mistry.platform.dto.ResetPasswordRequest;
import com.mistry.platform.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = customerService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = customerService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        ForgotPasswordResponse response = customerService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        customerService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. You can now log in with your new password."));
    }
}
