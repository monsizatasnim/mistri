package com.mistry.platform.controller;

import com.mistry.platform.dto.ProductResponse;
import com.mistry.platform.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopProductController {

    private final ProductService productService;

    @GetMapping("/{shopOwnerId}/products")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public ResponseEntity<List<ProductResponse>> getShopProducts(@PathVariable Long shopOwnerId) {
        return ResponseEntity.ok(productService.getProductsOfShop(shopOwnerId));
    }
}
