package com.mistry.platform.controller;

import com.mistry.platform.dto.ProductRequest;
import com.mistry.platform.dto.ProductResponse;
import com.mistry.platform.dto.StockStatusRequest;
import com.mistry.platform.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop-owner/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHOP_OWNER')")
public class ShopOwnerProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            Authentication authentication,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.addProduct(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getMyProducts(Authentication authentication) {
        return ResponseEntity.ok(productService.getMyProducts(authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(authentication.getName(), id, request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStock(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody StockStatusRequest request) {
        return ResponseEntity.ok(productService.updateStockStatus(authentication.getName(), id, request));
    }
}
