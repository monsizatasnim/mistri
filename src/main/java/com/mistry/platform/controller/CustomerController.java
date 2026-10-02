package com.mistry.platform.controller;

import com.mistry.platform.dto.ProfileResponse;
import com.mistry.platform.dto.UpdateProfileRequest;
import com.mistry.platform.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/portal")
    public ResponseEntity<Map<String, String>> portal(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to the customer portal",
                "loggedInAs", authentication.getName()
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        ProfileResponse response = customerService.getProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = customerService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }
}
