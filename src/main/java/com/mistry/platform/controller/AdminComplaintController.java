package com.mistry.platform.controller;

import com.mistry.platform.dto.AdminComplaintDetailResponse;
import com.mistry.platform.dto.ComplaintResponse;
import com.mistry.platform.dto.ResolveComplaintRequest;
import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getComplaints(
            @RequestParam(required = false) ComplaintStatus status) {
        return ResponseEntity.ok(complaintService.getComplaintsForAdmin(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminComplaintDetailResponse> getComplaintDetail(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintDetailForAdmin(id));
    }

    @PutMapping("/{id}/start-review")
    public ResponseEntity<ComplaintResponse> startReview(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.startReview(id));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ComplaintResponse> resolve(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ResolveComplaintRequest request) {
        return ResponseEntity.ok(complaintService.resolve(authentication.getName(), id, request));
    }
}