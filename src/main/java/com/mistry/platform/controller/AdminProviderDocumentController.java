package com.mistry.platform.controller;

import com.mistry.platform.dto.ProviderDocumentsResponse;
import com.mistry.platform.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/providers")
public class AdminProviderDocumentController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    @GetMapping("/{id}/documents")
    public ResponseEntity<ProviderDocumentsResponse> providerDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(serviceProviderService.getDocuments(id));
    }
}
