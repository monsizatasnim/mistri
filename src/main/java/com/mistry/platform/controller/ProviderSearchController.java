package com.mistry.platform.controller;

import com.mistry.platform.dto.ProviderSearchResponse;
import com.mistry.platform.service.ProviderSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderSearchController {

    private final ProviderSearchService providerSearchService;

    public ProviderSearchController(ProviderSearchService providerSearchService) {
        this.providerSearchService = providerSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProviderSearchResponse>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(providerSearchService.search(category, location));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderSearchResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(providerSearchService.getProfile(id));
    }
}