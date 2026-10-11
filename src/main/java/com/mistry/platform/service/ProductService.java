package com.mistry.platform.service;

import com.mistry.platform.dto.ProductRequest;
import com.mistry.platform.dto.ProductResponse;
import com.mistry.platform.dto.StockStatusRequest;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.Product;
import com.mistry.platform.entity.ProductAvailabilityType;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.InvalidProductException;
import com.mistry.platform.exception.ProductNotFoundException;
import com.mistry.platform.repository.ProductRepository;
import com.mistry.platform.repository.ShopOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopOwnerRepository shopOwnerRepository;

    private ShopOwner findShopOwner(String identifier) {
        return shopOwnerRepository.findByEmail(identifier)
                .or(() -> shopOwnerRepository.findByPhone(identifier))
                .orElseThrow(() -> new BadCredentialsException("Shop owner account not found"));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getShopOwnerId(),
                p.getName(),
                p.getCategory(),
                p.getDescription(),
                p.getImageUrl(),
                p.getAvailabilityType(),
                p.getSalePrice(),
                p.getRentPrice(),
                p.getStockStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt());
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private void applyRequest(Product product, ProductRequest request) {
        ProductAvailabilityType type = request.getAvailabilityType();
        boolean needsSale = type == ProductAvailabilityType.SALE || type == ProductAvailabilityType.BOTH;
        boolean needsRent = type == ProductAvailabilityType.RENT || type == ProductAvailabilityType.BOTH;

        if (needsSale && !isPositive(request.getSalePrice())) {
            throw new InvalidProductException("A sale price greater than zero is required for this product");
        }
        if (needsRent && !isPositive(request.getRentPrice())) {
            throw new InvalidProductException("A rent price greater than zero is required for this product");
        }

        product.setName(request.getName().trim());
        product.setCategory(clean(request.getCategory()));
        product.setDescription(clean(request.getDescription()));
        product.setImageUrl(clean(request.getImageUrl()));
        product.setAvailabilityType(type);
        product.setSalePrice(needsSale ? request.getSalePrice() : null);
        product.setRentPrice(needsRent ? request.getRentPrice() : null);
        product.setUpdatedAt(LocalDateTime.now());
    }

    public ProductResponse addProduct(String loggedInIdentifier, ProductRequest request) {
        ShopOwner shopOwner = findShopOwner(loggedInIdentifier);

        Product product = new Product();
        product.setShopOwnerId(shopOwner.getId());
        applyRequest(product, request);
        product.setCreatedAt(LocalDateTime.now());

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(String loggedInIdentifier, Long productId, ProductRequest request) {
        ShopOwner shopOwner = findShopOwner(loggedInIdentifier);
        Product product = productRepository.findByIdAndShopOwnerId(productId, shopOwner.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        applyRequest(product, request);

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateStockStatus(String loggedInIdentifier, Long productId, StockStatusRequest request) {
        ShopOwner shopOwner = findShopOwner(loggedInIdentifier);
        Product product = productRepository.findByIdAndShopOwnerId(productId, shopOwner.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        product.setStockStatus(request.getStockStatus());
        product.setUpdatedAt(LocalDateTime.now());

        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> getMyProducts(String loggedInIdentifier) {
        ShopOwner shopOwner = findShopOwner(loggedInIdentifier);
        return productRepository.findByShopOwnerIdOrderByCreatedAtDesc(shopOwner.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProductResponse> getProductsOfShop(Long shopOwnerId) {
        ShopOwner shopOwner = shopOwnerRepository.findById(shopOwnerId)
                .filter(s -> s.getVerificationStatus() == VerificationStatus.VERIFIED)
                .filter(s -> s.getAccountStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException("Shop not found"));

        return productRepository.findByShopOwnerIdOrderByCreatedAtDesc(shopOwner.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }
}
