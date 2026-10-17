package com.mistry.platform.service;

import com.mistry.platform.dto.OrderStatusChangeResponse;
import com.mistry.platform.dto.OrderStatusUpdateRequest;
import com.mistry.platform.dto.ShopOwnerOrderItemResponse;
import com.mistry.platform.dto.ShopOwnerOrderResponse;
import com.mistry.platform.dto.UrgentDeliveryRequest;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOrder;
import com.mistry.platform.entity.ShopOrderItem;
import com.mistry.platform.entity.ShopOrderStatus;
import com.mistry.platform.entity.ShopOrderStatusChange;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOrderStatusChangeRepository;
import com.mistry.platform.repository.ShopOwnerOrderRepository;
import com.mistry.platform.repository.ShopOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShopOrderManagementService {

    private static final Set<ShopOrderStatus> FINISHED = EnumSet.of(
            ShopOrderStatus.COMPLETED, ShopOrderStatus.DECLINED, ShopOrderStatus.CANCELLED);

    private final ShopOwnerOrderRepository orderRepository;
    private final ShopOwnerRepository shopOwnerRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ShopOrderStatusChangeRepository historyRepository;

    // ------------------------------------------------------------ viewing

    @Transactional(readOnly = true)
    public List<ShopOwnerOrderResponse> list(String identifier, String status) {
        ShopOwner shop = findShop(identifier);
        ShopOrderStatus wanted = parseOptionalStatus(status);
        return orderRepository.findByShopOwnerIdOrderByCreatedAtDesc(shop.getId()).stream()
                .filter(o -> wanted == null || o.getStatus() == wanted)
                .map(this::toResponse)
                .toList();
    }

    /** Completed, declined and cancelled orders. */
    @Transactional(readOnly = true)
    public List<ShopOwnerOrderResponse> history(String identifier) {
        ShopOwner shop = findShop(identifier);
        return orderRepository.findByShopOwnerIdOrderByCreatedAtDesc(shop.getId()).stream()
                .filter(o -> FINISHED.contains(o.getStatus()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShopOwnerOrderResponse get(String identifier, Long orderId) {
        ShopOwner shop = findShop(identifier);
        return toResponse(findOrder(shop, orderId));
    }

    // ------------------------------------------------------------ deciding

    @Transactional
    public ShopOwnerOrderResponse accept(String identifier, Long orderId) {
        ShopOrder order = findOrder(findShop(identifier), orderId);
        if (order.getStatus() != ShopOrderStatus.PLACED) {
            throw conflict("Only a new order can be accepted. This order is " + order.getStatus() + ".");
        }
        return change(order, ShopOrderStatus.ACCEPTED, "Accepted by the shop");
    }

    @Transactional
    public ShopOwnerOrderResponse decline(String identifier, Long orderId, String reason) {
        ShopOrder order = findOrder(findShop(identifier), orderId);
        if (order.getStatus() != ShopOrderStatus.PLACED) {
            throw conflict("Only a new order can be declined. This order is " + order.getStatus() + ".");
        }
        String cleaned = reason == null ? "" : reason.trim();
        if (cleaned.isEmpty()) {
            throw bad("Please give a reason for declining.");
        }
        order.setDeclineReason(cleaned);
        return change(order, ShopOrderStatus.DECLINED, "Declined: " + cleaned);
    }

    // ------------------------------------------------------------ progress

    @Transactional
    public ShopOwnerOrderResponse updateStatus(String identifier, Long orderId, OrderStatusUpdateRequest request) {
        ShopOrder order = findOrder(findShop(identifier), orderId);
        ShopOrderStatus next = parseRequiredStatus(request.status());
        ShopOrderStatus current = order.getStatus();

        if (!allowedNext(current).contains(next)) {
            throw conflict("An order that is " + current + " cannot be changed to " + next + ".");
        }
        if (request.fastDeliveryPossible() != null) {
            applyUrgentAnswer(order, request.fastDeliveryPossible(), request.estimatedMinutes());
        }
        return change(order, next, null);
    }

    @Transactional
    public ShopOwnerOrderResponse setUrgentDelivery(String identifier, Long orderId, UrgentDeliveryRequest request) {
        ShopOrder order = findOrder(findShop(identifier), orderId);
        Set<ShopOrderStatus> open = EnumSet.of(
                ShopOrderStatus.PLACED, ShopOrderStatus.ACCEPTED, ShopOrderStatus.PREPARING);
        if (!open.contains(order.getStatus())) {
            throw conflict("The delivery answer can only be given while the order is open. This order is "
                    + order.getStatus() + ".");
        }
        applyUrgentAnswer(order, request.fastDeliveryPossible(), request.estimatedMinutes());
        String note = request.fastDeliveryPossible()
                ? "Fast delivery is possible, about " + request.estimatedMinutes() + " minutes"
                : "Fast delivery is not possible";
        order.setStatus(order.getStatus());
        orderRepository.save(order);
        historyRepository.save(new ShopOrderStatusChange(order.getId(), order.getStatus().name(), note));
        return toResponse(order);
    }

    @Transactional
    public ShopOwnerOrderResponse markItemUnavailable(String identifier, Long orderId, Long itemId) {
        ShopOrder order = findOrder(findShop(identifier), orderId);
        Set<ShopOrderStatus> allowed = EnumSet.of(
                ShopOrderStatus.PLACED, ShopOrderStatus.ACCEPTED, ShopOrderStatus.MODIFIED);
        if (!allowed.contains(order.getStatus())) {
            throw conflict("Items can only be marked unavailable before preparation starts. This order is "
                    + order.getStatus() + ".");
        }

        ShopOrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Item not found in this order."));
        if (item.isUnavailable()) {
            throw conflict("This item is already marked unavailable.");
        }
        boolean othersLeft = order.getItems().stream()
                .anyMatch(i -> !i.isUnavailable() && !i.getId().equals(itemId));
        if (!othersLeft) {
            throw conflict("Every item would be unavailable. Please decline the order instead.");
        }

        item.setUnavailable(true);
        order.setTotalAmount(availableTotal(order));
        return change(order, ShopOrderStatus.MODIFIED, item.getProductName()
                + " is unavailable. Waiting for the provider to accept a partial order or cancel.");
    }

    // ------------------------------------------------------------ helpers

    private ShopOwner findShop(String identifier) {
        return shopOwnerRepository.findByEmail(identifier)
                .or(() -> shopOwnerRepository.findByPhone(identifier))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN, "Shop owner account not found."));
    }

    private ShopOrder findOrder(ShopOwner shop, Long orderId) {
        return orderRepository.findByIdAndShopOwnerId(orderId, shop.getId())
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Order not found."));
    }

    private Set<ShopOrderStatus> allowedNext(ShopOrderStatus current) {
        return switch (current) {
            case ACCEPTED -> EnumSet.of(ShopOrderStatus.PREPARING);
            case PREPARING -> EnumSet.of(ShopOrderStatus.READY, ShopOrderStatus.OUT_FOR_DELIVERY);
            case READY, OUT_FOR_DELIVERY -> EnumSet.of(ShopOrderStatus.COMPLETED);
            default -> EnumSet.noneOf(ShopOrderStatus.class);
        };
    }

    private void applyUrgentAnswer(ShopOrder order, boolean fastPossible, Integer minutes) {
        if (!order.isUrgent()) {
            throw bad("Only an urgent order needs a fast-delivery answer.");
        }
        if (fastPossible && minutes == null) {
            throw bad("Please give an estimated time in minutes.");
        }
        order.setFastDeliveryPossible(fastPossible);
        order.setEstimatedMinutes(minutes);
    }

    private BigDecimal availableTotal(ShopOrder order) {
        BigDecimal total = BigDecimal.ZERO;
        for (ShopOrderItem i : order.getItems()) {
            if (!i.isUnavailable()) {
                total = total.add(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            }
        }
        return total;
    }

    private ShopOwnerOrderResponse change(ShopOrder order, ShopOrderStatus next, String note) {
        order.setStatus(next);
        ShopOrder saved = orderRepository.save(order);
        historyRepository.save(new ShopOrderStatusChange(saved.getId(), next.name(), note));
        return toResponse(saved);
    }

    private ShopOrderStatus parseRequiredStatus(String value) {
        ShopOrderStatus parsed = parseOptionalStatus(value);
        if (parsed == null) {
            throw bad("Status is required.");
        }
        return parsed;
    }

    private ShopOrderStatus parseOptionalStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ShopOrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw bad("Unknown status: " + value);
        }
    }

    private ShopOrderException bad(String message) {
        return new ShopOrderException(HttpStatus.BAD_REQUEST, message);
    }

    private ShopOrderException conflict(String message) {
        return new ShopOrderException(HttpStatus.CONFLICT, message);
    }

    private ShopOwnerOrderResponse toResponse(ShopOrder order) {
        ServiceProvider provider = serviceProviderRepository.findById(order.getProviderId()).orElse(null);

        List<ShopOwnerOrderItemResponse> items = order.getItems().stream()
                .map(i -> new ShopOwnerOrderItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getProductName(),
                        i.getTerm() == null ? null : i.getTerm().name(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())),
                        i.isUnavailable()))
                .toList();

        List<OrderStatusChangeResponse> history = new ArrayList<>();
        history.add(new OrderStatusChangeResponse("PLACED", null, order.getCreatedAt()));
        historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(order.getId())
                .forEach(h -> history.add(new OrderStatusChangeResponse(h.getStatus(), h.getNote(), h.getChangedAt())));

        return new ShopOwnerOrderResponse(
                order.getId(),
                order.getProviderId(),
                provider == null ? null : provider.getFullName(),
                provider == null ? null : provider.getPhone(),
                order.isUrgent(),
                order.getDeliveryLocation(),
                order.getNote(),
                order.getStatus(),
                order.getDeclineReason(),
                order.getFastDeliveryPossible(),
                order.getEstimatedMinutes(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items,
                history);
    }
}
