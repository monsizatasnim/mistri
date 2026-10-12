package com.mistry.platform.controller;

import com.mistry.platform.dto.ComplaintRequest;
import com.mistry.platform.dto.ComplaintResponse;
import com.mistry.platform.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ComplaintResponse> submit(
            Authentication authentication,
            @Valid @RequestBody ComplaintRequest request) {
        ComplaintResponse response = complaintService.submit(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getMyComplaints(Authentication authentication) {
        return ResponseEntity.ok(complaintService.getMyComplaints(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getMyComplaint(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getMyComplaint(authentication.getName(), id));
    }
}
