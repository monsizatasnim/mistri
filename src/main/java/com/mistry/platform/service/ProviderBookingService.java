package com.mistry.platform.service;

import com.mistry.platform.dto.BookingRequestResponse;
import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.repository.ProviderBookingRequestRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProviderBookingService {

    private static final Set<String> TAKEN = Set.of("CONFIRMED", "IN_PROGRESS");

    private final ProviderBookingRequestRepository bookingRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<BookingRequestResponse> list(String identifier) {
        ServiceProvider provider = findProvider(identifier);
        return bookingRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BookingRequestResponse accept(String identifier, Long id) {
        ServiceProvider provider = findProvider(identifier);
        BookingEntity b = find(provider, id);
        requireStatus(b, "PENDING", "accepted");
        if (b.getAppointmentTime() != null) {
            boolean clash = bookingRepository.findByProviderIdOrderByCreatedAtDesc(provider.getId()).stream()
                    .anyMatch(o -> !o.getId().equals(b.getId())
                            && b.getAppointmentTime().equals(o.getAppointmentTime())
                            && TAKEN.contains(o.getStatus()));
            if (clash) {
                throw conflict("You already have a confirmed booking at this time.");
            }
        }
        b.setStatus("CONFIRMED");
        return toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingRequestResponse decline(String identifier, Long id) {
        BookingEntity b = find(findProvider(identifier), id);
        requireStatus(b, "PENDING", "declined");
        b.setStatus("DECLINED");
        return toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingRequestResponse suggestTime(String identifier, Long id, LocalDateTime newTime) {
        BookingEntity b = find(findProvider(identifier), id);
        requireStatus(b, "PENDING", "given a new time");
        if (!newTime.isAfter(LocalDateTime.now())) {
            throw new ShopOrderException(HttpStatus.BAD_REQUEST, "The new time must be in the future.");
        }
        b.setSuggestedTime(newTime);
        b.setStatus("SUGGESTED_ALTERNATIVE");
        return toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingRequestResponse start(String identifier, Long id) {
        BookingEntity b = find(findProvider(identifier), id);
        requireStatus(b, "CONFIRMED", "started");
        b.setStatus("IN_PROGRESS");
        return toResponse(bookingRepository.save(b));
    }

    @Transactional
    public BookingRequestResponse complete(String identifier, Long id) {
        BookingEntity b = find(findProvider(identifier), id);
        requireStatus(b, "IN_PROGRESS", "completed");
        b.setStatus("COMPLETED");
        return toResponse(bookingRepository.save(b));
    }

    private ServiceProvider findProvider(String identifier) {
        return serviceProviderRepository.findByEmail(identifier)
                .or(() -> serviceProviderRepository.findByPhone(identifier))
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN, "Service provider account not found."));
    }

    private BookingEntity find(ServiceProvider provider, Long id) {
        return bookingRepository.findByIdAndProviderId(id, provider.getId())
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Booking not found."));
    }

    private void requireStatus(BookingEntity b, String needed, String action) {
        if (!needed.equals(b.getStatus())) {
            throw conflict("Only a " + needed + " booking can be " + action + ". This one is " + b.getStatus() + ".");
        }
    }

    private ShopOrderException conflict(String message) {
        return new ShopOrderException(HttpStatus.CONFLICT, message);
    }

    private String customerName(Long customerId) {
        if (customerId == null) {
            return null;
        }
        return customerRepository.findById(customerId).map(c -> c.getFullName()).orElse(null);
    }

    private BookingRequestResponse toResponse(BookingEntity b) {
        return new BookingRequestResponse(b.getId(), b.getCustomerId(), b.getServiceName(), b.getStatus(),
                b.getAppointmentTime(), b.getSuggestedTime(), b.getCreatedAt(), customerName(b.getCustomerId()), b.getRefundPercent());
    }
}
