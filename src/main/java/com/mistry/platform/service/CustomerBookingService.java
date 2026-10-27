package com.mistry.platform.service;

import com.mistry.platform.dto.BookingRequestResponse;
import com.mistry.platform.dto.CustomerBookingRequest;
import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.entity.Customer;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.BookingRepository;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerBookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    @Transactional
    public BookingRequestResponse create(String identifier, CustomerBookingRequest request) {
        Customer customer = findCustomer(identifier);

        if (!serviceProviderRepository.existsById(request.providerId())) {
            throw new ShopOrderException(HttpStatus.NOT_FOUND, "Service provider not found.");
        }
        if (!request.appointmentTime().isAfter(LocalDateTime.now())) {
            throw new ShopOrderException(HttpStatus.BAD_REQUEST, "The appointment time must be in the future.");
        }

        String serviceName = request.serviceName();
        if (serviceName == null || serviceName.isBlank()) {
            serviceName = "Service booking";
        }

        BookingEntity booking = new BookingEntity(customer.getId(), serviceName.trim(), "PENDING");
        booking.setProviderId(request.providerId());
        booking.setAppointmentTime(request.appointmentTime());
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingRequestResponse> list(String identifier) {
        Customer customer = findCustomer(identifier);
        return bookingRepository.findByCustomerId(customer.getId()).stream()
                .sorted(Comparator.comparing(BookingEntity::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private Customer findCustomer(String identifier) {
        return customerRepository.findByEmail(identifier)
                .or(() -> customerRepository.findByPhone(identifier))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN, "Customer account not found."));
    }

    private BookingRequestResponse toResponse(BookingEntity b) {
        return new BookingRequestResponse(b.getId(), b.getCustomerId(), b.getServiceName(), b.getStatus(),
                b.getAppointmentTime(), b.getSuggestedTime(), b.getCreatedAt());
    }
}
