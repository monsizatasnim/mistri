package com.mistry.platform.controller;

import com.mistry.platform.dto.CreateBookingRequest;
import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.BookingRepository;
import com.mistry.platform.repository.CustomerRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import com.mistry.platform.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public BookingController(BookingService bookingService,
                             BookingRepository bookingRepository,
                             CustomerRepository customerRepository,
                             ServiceProviderRepository serviceProviderRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingEntity> createBooking(Authentication auth,
                                                       @RequestBody CreateBookingRequest request) {
        Long customerId = currentCustomerId(auth);
        BookingEntity booking = bookingService.bookSlot(customerId, "General Service", "PENDING");
        if (request.getProviderId() != null || request.getAppointmentTime() != null) {
            booking.setProviderId(request.getProviderId());
            booking.setAppointmentTime(request.getAppointmentTime());
            booking = bookingRepository.save(booking);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingEntity>> getCustomerBookings(Authentication auth,
                                                                   @PathVariable Long customerId) {
        if (!customerId.equals(currentCustomerId(auth))) {
            throw new ShopOrderException(HttpStatus.FORBIDDEN, "You can only see your own bookings.");
        }
        return ResponseEntity.ok(bookingService.getBookingsForCustomer(customerId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingEntity>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingEntity> startBooking(Authentication auth, @PathVariable Long id) {
        requireProviderOwns(auth, id);
        return ResponseEntity.ok(bookingService.startBooking(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<BookingEntity> completeBooking(Authentication auth, @PathVariable Long id) {
        requireProviderOwns(auth, id);
        return ResponseEntity.ok(bookingService.completeBooking(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingEntity> cancelBooking(Authentication auth, @PathVariable Long id) {
        requireCustomerOwns(auth, id);
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingEntity> rescheduleBooking(Authentication auth, @PathVariable Long id) {
        requireCustomerOwns(auth, id);
        return ResponseEntity.ok(bookingService.rescheduleBooking(id));
    }

    private Long currentCustomerId(Authentication auth) {
        String name = auth.getName();
        return customerRepository.findByEmail(name)
                .or(() -> customerRepository.findByPhone(name))
                .map(c -> c.getId())
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN, "Customer account not found."));
    }

    private Long currentProviderId(Authentication auth) {
        String name = auth.getName();
        return serviceProviderRepository.findByEmail(name)
                .or(() -> serviceProviderRepository.findByPhone(name))
                .map(p -> p.getId())
                .orElseThrow(() -> new ShopOrderException(HttpStatus.FORBIDDEN, "Service provider account not found."));
    }

    private void requireCustomerOwns(Authentication auth, Long bookingId) {
        Long me = currentCustomerId(auth);
        BookingEntity b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (!me.equals(b.getCustomerId())) {
            throw new ShopOrderException(HttpStatus.NOT_FOUND, "Booking not found.");
        }
    }

    private void requireProviderOwns(Authentication auth, Long bookingId) {
        Long me = currentProviderId(auth);
        BookingEntity b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (!me.equals(b.getProviderId())) {
            throw new ShopOrderException(HttpStatus.NOT_FOUND, "Booking not found.");
        }
    }

    @ExceptionHandler(ShopOrderException.class)
    public ResponseEntity<Map<String, String>> handle(ShopOrderException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
