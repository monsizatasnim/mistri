package com.mistry.platform.service;

import com.mistry.platform.dto.ShopOrderResponse;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOrder;
import com.mistry.platform.entity.ShopOrderStatus;
import com.mistry.platform.entity.ShopOrderStatusChange;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOrderStatusChangeRepository;
import com.mistry.platform.repository.ShopOwnerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderOrderReplyService {

    private final ShopOwnerOrderRepository orderRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ShopOrderStatusChangeRepository historyRepository;
    private final ShopOrderService shopOrderService;

    @Transactional
    public ShopOrderResponse acceptPartial(String identifier, Long orderId) {
        ShopOrder order = findModifiedOrder(identifier, orderId);
        order.setStatus(ShopOrderStatus.ACCEPTED);
        orderRepository.save(order);
        historyRepository.save(new ShopOrderStatusChange(order.getId(), "ACCEPTED",
                "Provider accepted the partial order"));
        return shopOrderService.myOrder(identifier, orderId);
    }

    @Transactional
    public ShopOrderResponse cancel(String identifier, Long orderId) {
        ShopOrder order = findModifiedOrder(identifier, orderId);
        order.setStatus(ShopOrderStatus.CANCELLED);
        orderRepository.save(order);
        historyRepository.save(new ShopOrderStatusChange(order.getId(), "CANCELLED",
                "Provider cancelled the rest of the order"));
        return shopOrderService.myOrder(identifier, orderId);
    }

    private ShopOrder findModifiedOrder(String identifier, Long orderId) {
        ServiceProvider provider = serviceProviderRepository.findByEmail(identifier)
                .or(() -> serviceProviderRepository.findByPhone(identifier))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN,
                        "Service provider account not found."));
        ShopOrder order = orderRepository.findById(orderId)
                .filter(o -> o.getProviderId().equals(provider.getId()))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Order not found."));
        if (order.getStatus() != ShopOrderStatus.MODIFIED) {
            throw new ShopOrderException(HttpStatus.CONFLICT,
                    "Only an order the shop has changed can be answered. This order is "
                            + order.getStatus() + ".");
        }
        return order;
    }
}
