package com.mistry.platform.service;

import com.mistry.platform.entity.Booking;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.entity.CustomerPayment;
import com.mistry.platform.repository.CustomerBookingHistoryRepository;
import com.mistry.platform.repository.CustomerPaymentRepository;
import com.mistry.platform.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerPaymentHistoryService {

    @Autowired
    private CustomerPaymentRepository paymentRepository;

    @Autowired
    private CustomerBookingHistoryRepository bookingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public Long customerId(String identifier) {
        Optional<Customer> c = customerRepository.findByEmail(identifier);
        if (c.isEmpty()) {
            c = customerRepository.findByPhone(identifier);
        }
        return c.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")).getId();
    }

    // Payment shesh hole eta call korle history te nijei joma hoy
    public CustomerPayment recordPayment(Long customerId, Long bookingId, double amount,
                                         String method, boolean success) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }
        CustomerPayment p = new CustomerPayment();
        p.setCustomerId(customerId);
        p.setBookingId(bookingId);
        p.setAmount(amount);
        p.setPaymentMethod(method);
        p.setStatus(success ? "SUCCESS" : "FAILED");
        p.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return paymentRepository.save(p);
    }

    public List<CustomerPayment> myPayments(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public Map<String, Object> receipt(Long customerId, Long paymentId) {
        CustomerPayment p = paymentRepository.findById(paymentId)
                .filter(x -> x.getCustomerId().equals(customerId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("receiptNo", "RCPT-" + p.getId());
        map.put("transactionRef", p.getTransactionRef());
        map.put("bookingId", p.getBookingId());
        map.put("amount", p.getAmount());
        map.put("paymentMethod", p.getPaymentMethod());
        map.put("status", p.getStatus());
        map.put("paidAt", p.getCreatedAt());
        return map;
    }

    public List<Map<String, Object>> serviceHistory(Long customerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking b : bookingRepository.findByCustomerIdOrderByAppointmentTimeDesc(customerId)) {
            boolean past = "COMPLETED".equalsIgnoreCase(b.getStatus())
                    || b.getAppointmentTime().isBefore(LocalDateTime.now());
            if (past) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("bookingId", b.getBookingId());
                m.put("providerId", b.getProviderId());
                m.put("appointmentTime", b.getAppointmentTime());
                m.put("status", b.getStatus());
                list.add(m);
            }
        }
        return list;
    }
}