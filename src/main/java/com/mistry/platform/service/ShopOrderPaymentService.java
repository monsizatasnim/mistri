package com.mistry.platform.service;

import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.ShopOrder;
import com.mistry.platform.entity.ShopOrderPayment;
import com.mistry.platform.entity.ShopOrderStatus;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.repository.ShopOrderPaymentRepository;
import com.mistry.platform.repository.ShopOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ShopOrderPaymentService {

    private static final List<String> METHODS = List.of("CASH", "BKASH", "BANK_TRANSFER");

    private static final Set<ShopOrderStatus> PAYABLE = EnumSet.of(
            ShopOrderStatus.ACCEPTED, ShopOrderStatus.PREPARING, ShopOrderStatus.READY,
            ShopOrderStatus.OUT_FOR_DELIVERY, ShopOrderStatus.COMPLETED);

    @Autowired
    private ShopOrderRepository orderRepository;

    @Autowired
    private ShopOrderPaymentRepository paymentRepository;

    @Autowired
    private ServiceProviderRepository providerRepository;

    private Long providerId(String email) {
        ServiceProvider p = providerRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
        return p.getId();
    }

    private ShopOrder ownOrder(Long providerId, Long orderId) {
        return orderRepository.findByIdAndProviderId(orderId, providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    public List<String> methods() {
        return METHODS;
    }

    // Total amount + payment status dekhar jonno
    public Map<String, Object> summary(String email, Long orderId) {
        Long pid = providerId(email);
        ShopOrder order = ownOrder(pid, orderId);
        Optional<ShopOrderPayment> pay = paymentRepository.findByOrderId(orderId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("orderId", order.getId());
        map.put("orderStatus", order.getStatus());
        map.put("totalAmount", order.getTotalAmount());
        map.put("availableMethods", METHODS);
        map.put("paymentStatus", pay.map(ShopOrderPayment::getStatus).orElse("NOT_PAID"));
        pay.ifPresent(p -> {
            map.put("paymentMethod", p.getPaymentMethod());
            map.put("transactionRef", p.getTransactionRef());
            map.put("paidAt", p.getCreatedAt());
        });
        return map;
    }

    public ShopOrderPayment pay(String email, Long orderId, String method) {
        Long pid = providerId(email);
        ShopOrder order = ownOrder(pid, orderId);

        String m = method == null ? "" : method.trim().toUpperCase();
        if (!METHODS.contains(m)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment method must be one of " + METHODS);
        }
        if (!PAYABLE.contains(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This order cannot be paid yet. The shop must accept it first.");
        }
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order already has a payment.");
        }

        ShopOrderPayment p = new ShopOrderPayment();
        p.setOrderId(orderId);
        p.setProviderId(pid);
        p.setAmount(order.getTotalAmount()); // amount order theke, provider vul dite parbe na
        p.setPaymentMethod(m);
        // Cash hole shop owner pawar por Paid hoy, tai ekhon pending
        p.setStatus("CASH".equals(m) ? "PENDING_CASH" : "PAID");
        p.setTransactionRef("SOP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return paymentRepository.save(p);
    }

    public List<ShopOrderPayment> history(String email) {
        return paymentRepository.findByProviderIdOrderByCreatedAtDesc(providerId(email));
    }
}