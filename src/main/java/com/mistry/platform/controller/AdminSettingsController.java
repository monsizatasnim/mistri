package com.mistry.platform.controller;

import com.mistry.platform.dto.PlatformSettingsRequest;
import com.mistry.platform.dto.PlatformSettingsResponse;
import com.mistry.platform.dto.SettingsAuditResponse;
import com.mistry.platform.exception.SettingsValidationException;
import com.mistry.platform.service.PlatformSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final PlatformSettingsService settingsService;

    public AdminSettingsController(PlatformSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public PlatformSettingsResponse getSettings() {
        return PlatformSettingsResponse.from(settingsService.getSettings());
    }

    @PutMapping
    public PlatformSettingsResponse updateSettings(@RequestBody PlatformSettingsRequest request,
                                                   Authentication authentication) {
        String admin = authentication == null ? null : authentication.getName();
        return PlatformSettingsResponse.from(settingsService.update(request, admin));
    }

    @GetMapping("/audit")
    public List<SettingsAuditResponse> getAuditLog() {
        return settingsService.getAuditLog().stream().map(SettingsAuditResponse::from).toList();
    }

    @ExceptionHandler(SettingsValidationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(SettingsValidationException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Platform settings are invalid.");
        body.put("errors", e.getErrors());
        return ResponseEntity.badRequest().body(body);
    }
}