package com.mistry.platform.controller;

import com.mistry.platform.dto.ComplaintResponse;
import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
