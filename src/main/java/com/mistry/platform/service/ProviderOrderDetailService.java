package com.mistry.platform.service;

import com.mistry.platform.dto.OrderStatusChangeResponse;
import com.mistry.platform.dto.ProviderOrderDetailResponse;
import com.mistry.platform.dto.ShopOwnerOrderItemResponse;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOrder;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderOrderDetailService {

    private final ShopOwnerOrderRepository orderRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ShopOwnerRepository shopOwnerRepository;
    private final ShopOrderStatusChangeRepository historyRepository;

    @Transactional(readOnly = true)
    public ProviderOrderDetailResponse detail(String identifier, Long orderId) {
        ServiceProvider provider = serviceProviderRepository.findByEmail(identifier)
                .or(() -> serviceProviderRepository.findByPhone(identifier))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN,
                        "Service provider account not found."));
        ShopOrder order = orderRepository.findById(orderId)
                .filter(o -> o.getProviderId().equals(provider.getId()))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Order not found."));

        String shopName = shopOwnerRepository.findById(order.getShopOwnerId())
                .map(ShopOwner::getShopName).orElse(null);

        List<ShopOwnerOrderItemResponse> items = order.getItems().stream()
                .map(i -> new ShopOwnerOrderItemResponse(
                        i.getId(), i.getProductId(), i.getProductName(),
                        i.getTerm() == null ? null : i.getTerm().name(),
                        i.getQuantity(), i.getUnitPrice(),
                        i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())),
                        i.isUnavailable()))
                .toList();

        List<OrderStatusChangeResponse> history = new ArrayList<>();
        history.add(new OrderStatusChangeResponse("PLACED", null, order.getCreatedAt()));
        historyRepository.findByOrderIdOrderByChangedAtAscIdAsc(order.getId())
                .forEach(h -> history.add(new OrderStatusChangeResponse(h.getStatus(), h.getNote(), h.getChangedAt())));

        return new ProviderOrderDetailResponse(
                order.getId(), order.getShopOwnerId(), shopName, order.isUrgent(),
                order.getDeliveryLocation(), order.getNote(), order.getStatus(),
                order.getDeclineReason(), order.getFastDeliveryPossible(),
                order.getEstimatedMinutes(), order.getTotalAmount(), order.getCreatedAt(),
                items, history);
    }
}
