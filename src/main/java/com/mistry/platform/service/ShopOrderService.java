package com.mistry.platform.service;

import com.mistry.platform.dto.PlaceShopOrderRequest;
import com.mistry.platform.dto.ShopOrderItemRequest;
import com.mistry.platform.dto.ShopOrderItemResponse;
import com.mistry.platform.dto.ShopOrderResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.OrderTerm;
import com.mistry.platform.entity.Product;
import com.mistry.platform.entity.ProductAvailabilityType;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOrder;
import com.mistry.platform.entity.ShopOrderItem;
import com.mistry.platform.entity.ShopOrderStatus;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.ShopSettings;
import com.mistry.platform.entity.StockStatus;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.ProductRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOrderRepository;
import com.mistry.platform.repository.ShopOwnerRepository;
import com.mistry.platform.repository.ShopSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShopOrderService {

    private static final int MAX_ITEMS = 50;
    private static final int MAX_QUANTITY = 1000;

    private final ShopOrderRepository orderRepository;
    private final ShopOwnerRepository shopOwnerRepository;
    private final ProductRepository productRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ShopSettingsRepository shopSettingsRepository;

    @Transactional
    public ShopOrderResponse placeOrder(String identifier, PlaceShopOrderRequest request) {
        ServiceProvider provider = findActiveProvider(identifier);

        if (request == null || request.shopOwnerId() == null) {
            throw bad("shopOwnerId is required.");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw bad("An order needs at least one item.");
        }
        if (request.items().size() > MAX_ITEMS) {
            throw bad("An order can have at most " + MAX_ITEMS + " items.");
        }

        ShopOwner shop = shopOwnerRepository.findById(request.shopOwnerId())
                .filter(s -> s.getVerificationStatus() == VerificationStatus.VERIFIED)
                .filter(s -> s.getAccountStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Shop not found."));

        ShopSettings settings = shopSettingsRepository.findByShopOwnerId(shop.getId()).orElse(null);
        if (settings != null && !settings.isAcceptingOnlineOrders()) {
            throw new ShopOrderException(HttpStatus.CONFLICT, "This shop is not taking online orders right now.");
        }

        ShopOrder order = new ShopOrder();
        order.setProviderId(provider.getId());
        order.setShopOwnerId(shop.getId());
        order.setUrgent(Boolean.TRUE.equals(request.urgent()));
        order.setDeliveryLocation(clean(request.deliveryLocation()));
        order.setNote(clean(request.note()));
        order.setStatus(ShopOrderStatus.PLACED);

        Set<String> seen = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ShopOrderItemRequest line : request.items()) {
            if (line == null || line.productId() == null || line.term() == null) {
                throw bad("Every item needs a productId and a term (SALE or RENT).");
            }
            if (line.quantity() == null || line.quantity() < 1 || line.quantity() > MAX_QUANTITY) {
                throw bad("Quantity must be between 1 and " + MAX_QUANTITY + ".");
            }
            if (!seen.add(line.productId() + ":" + line.term())) {
                throw bad("Product " + line.productId() + " is listed twice for " + line.term()
                        + ". Combine them into one item.");
            }

            Product product = productRepository.findByIdAndShopOwnerId(line.productId(), shop.getId())
                    .orElseThrow(() -> bad("Product " + line.productId() + " does not belong to this shop."));
            if (product.getStockStatus() != StockStatus.AVAILABLE) {
                throw new ShopOrderException(HttpStatus.CONFLICT, product.getName() + " is out of stock.");
            }

            BigDecimal unitPrice = priceFor(product, line.term());

            ShopOrderItem item = new ShopOrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setTerm(line.term());
            item.setQuantity(line.quantity());
            item.setUnitPrice(unitPrice);
            order.getItems().add(item);

            total = total.add(unitPrice.multiply(BigDecimal.valueOf(line.quantity())));
        }

        if (settings != null && settings.getProviderDiscountPercent() != null
                && settings.getProviderDiscountPercent().signum() > 0) {
            BigDecimal keep = BigDecimal.valueOf(100).subtract(settings.getProviderDiscountPercent());
            total = total.multiply(keep).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        order.setTotalAmount(total);
        return toResponse(orderRepository.save(order), shop.getShopName());
    }

    /** Flag an order that is still waiting for the shop as urgent. */
    @Transactional
    public ShopOrderResponse markUrgent(String identifier, Long orderId) {
        ServiceProvider provider = findActiveProvider(identifier);
        ShopOrder order = orderRepository.findByIdAndProviderId(orderId, provider.getId())
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Order not found."));
        if (order.getStatus() != ShopOrderStatus.PLACED) {
            throw new ShopOrderException(HttpStatus.CONFLICT,
                    "Only an order that is still waiting for the shop can be marked urgent.");
        }
        order.setUrgent(true);
        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<ShopOrderResponse> myOrders(String identifier) {
        ServiceProvider provider = findActiveProvider(identifier);
        return orderRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShopOrderResponse myOrder(String identifier, Long orderId) {
        ServiceProvider provider = findActiveProvider(identifier);
        return orderRepository.findByIdAndProviderId(orderId, provider.getId())
                .map(this::toResponse)
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Order not found."));
    }

    // ------------------------------------------------------------ helpers

    private ServiceProvider findActiveProvider(String identifier) {
        ServiceProvider provider = serviceProviderRepository.findByEmail(identifier)
                .or(() -> serviceProviderRepository.findByPhone(identifier))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN,
                        "Service provider account not found."));
        if (provider.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ShopOrderException(HttpStatus.FORBIDDEN, "Your account is not active.");
        }
        return provider;
    }

    private BigDecimal priceFor(Product product, OrderTerm term) {
        ProductAvailabilityType type = product.getAvailabilityType();
        if (term == OrderTerm.SALE) {
            boolean ok = (type == ProductAvailabilityType.SALE || type == ProductAvailabilityType.BOTH)
                    && product.getSalePrice() != null;
            if (!ok) {
                throw bad(product.getName() + " is not available for sale.");
            }
            return product.getSalePrice();
        }
        boolean ok = (type == ProductAvailabilityType.RENT || type == ProductAvailabilityType.BOTH)
                && product.getRentPrice() != null;
        if (!ok) {
            throw bad(product.getName() + " is not available for rent.");
        }
        return product.getRentPrice();
    }

    private ShopOrderException bad(String message) {
        return new ShopOrderException(HttpStatus.BAD_REQUEST, message);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ShopOrderResponse toResponse(ShopOrder order) {
        String shopName = shopOwnerRepository.findById(order.getShopOwnerId())
                .map(ShopOwner::getShopName)
                .orElse(null);
        return toResponse(order, shopName);
    }

    private ShopOrderResponse toResponse(ShopOrder order, String shopName) {
        List<ShopOrderItemResponse> items = order.getItems().stream()
                .map(i -> new ShopOrderItemResponse(
                        i.getProductId(),
                        i.getProductName(),
                        i.getTerm(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
                .toList();
        return new ShopOrderResponse(
                order.getId(),
                order.getShopOwnerId(),
                shopName,
                order.isUrgent(),
                order.getDeliveryLocation(),
                order.getNote(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items);
    }
}