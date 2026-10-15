package com.mistry.platform.controller;

import com.mistry.platform.dto.RankedProviderResponse;
import com.mistry.platform.service.ProviderRankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderRankingController {

    private final ProviderRankingService providerRankingService;

    public ProviderRankingController(ProviderRankingService providerRankingService) {
        this.providerRankingService = providerRankingService;
    }

    @GetMapping("/ranked")
    public ResponseEntity<List<RankedProviderResponse>> ranked(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String verificationStatus) {
        return ResponseEntity.ok(providerRankingService.rank(
                category, location, area, minPrice, maxPrice, verificationStatus));
    }
}
