package com.mistry.platform.controller;

import com.mistry.platform.entity.AvailabilitySlot;
import com.mistry.platform.service.ProviderAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
public class ProviderAvailabilityController {

    @Autowired
    private ProviderAvailabilityService availabilityService;

    @PostMapping("/api/service-provider/availability")
    public ResponseEntity<AvailabilitySlot> addSlot(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime
    ) {
        return ResponseEntity.ok(availabilityService.addSlot(authentication.getName(), date, startTime, endTime));
    }

    @PutMapping("/api/service-provider/availability/{slotId}")
    public ResponseEntity<AvailabilitySlot> editSlot(
            Authentication authentication,
            @PathVariable Long slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime
    ) {
        return ResponseEntity.ok(availabilityService.editSlot(authentication.getName(), slotId, date, startTime, endTime));
    }

    @DeleteMapping("/api/service-provider/availability/{slotId}")
    public ResponseEntity<Map<String, String>> removeSlot(Authentication authentication, @PathVariable Long slotId) {
        availabilityService.removeSlot(authentication.getName(), slotId);
        return ResponseEntity.ok(Map.of("message", "Slot removed successfully."));
    }

    @GetMapping("/api/service-provider/availability")
    public ResponseEntity<List<AvailabilitySlot>> mySlots(Authentication authentication) {
        return ResponseEntity.ok(availabilityService.mySlots(authentication.getName()));
    }

    @PutMapping("/api/service-provider/cancellation-policy")
    public ResponseEntity<Map<String, Object>> setPolicy(
            Authentication authentication,
            @RequestParam int windowHours,
            @RequestParam int refundPercent
    ) {
        return ResponseEntity.ok(availabilityService.setCancellationPolicy(authentication.getName(), windowHours, refundPercent));
    }

    @GetMapping("/api/providers/{providerId}/availability")
    public ResponseEntity<List<AvailabilitySlot>> providerSlots(@PathVariable Long providerId) {
        return ResponseEntity.ok(availabilityService.slotsOfProvider(providerId));
    }

    @GetMapping("/api/providers/{providerId}/cancellation-policy")
    public ResponseEntity<Map<String, Object>> providerPolicy(@PathVariable Long providerId) {
        return ResponseEntity.ok(availabilityService.getCancellationPolicy(providerId));
    }
}