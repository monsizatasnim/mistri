package com.mistry.platform.service;

import com.mistry.platform.entity.AvailabilitySlot;
import com.mistry.platform.entity.Booking;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.repository.AvailabilitySlotRepository;
import com.mistry.platform.repository.ProviderBookingLookupRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProviderAvailabilityService {

    @Autowired
    private AvailabilitySlotRepository slotRepository;

    @Autowired
    private ServiceProviderRepository providerRepository;

    @Autowired
    private ProviderBookingLookupRepository bookingLookupRepository;

    @Value("${platform.cancellation.min-window-hours:12}")
    private int minWindowHours;

    @Value("${platform.cancellation.min-refund-percent:25}")
    private int minRefundPercent;

    private ServiceProvider getProvider(String email) {
        return providerRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
    }

    private boolean isBooked(AvailabilitySlot slot) {
        LocalDateTime from = LocalDateTime.of(slot.getDate(), slot.getStartTime());
        LocalDateTime to = LocalDateTime.of(slot.getDate(), slot.getEndTime());
        List<Booking> bookings = bookingLookupRepository
                .findByProviderIdAndAppointmentTimeBetween(slot.getProviderId(), from, to);
        for (Booking b : bookings) {
            String status = b.getStatus() == null ? "" : b.getStatus().toUpperCase();
            if (!status.equals("DECLINED") && !status.equals("CANCELLED")) {
                return true;
            }
        }
        return false;
    }

    private void validateSlot(Long providerId, LocalDate date, LocalTime start, LocalTime end, Long ignoreSlotId) {
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date cannot be in the past.");
        }
        if (!end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time.");
        }
        for (AvailabilitySlot s : slotRepository.findByProviderIdAndDate(providerId, date)) {
            if (ignoreSlotId != null && s.getId().equals(ignoreSlotId)) continue;
            if (start.isBefore(s.getEndTime()) && end.isAfter(s.getStartTime())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This slot overlaps with an existing slot.");
            }
        }
    }

    public AvailabilitySlot addSlot(String email, LocalDate date, LocalTime start, LocalTime end) {
        ServiceProvider provider = getProvider(email);
        validateSlot(provider.getId(), date, start, end, null);
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setProviderId(provider.getId());
        slot.setDate(date);
        slot.setStartTime(start);
        slot.setEndTime(end);
        return slotRepository.save(slot);
    }

    public AvailabilitySlot editSlot(String email, Long slotId, LocalDate date, LocalTime start, LocalTime end) {
        ServiceProvider provider = getProvider(email);
        AvailabilitySlot slot = slotRepository.findByIdAndProviderId(slotId, provider.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
        if (isBooked(slot)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This slot is already booked. Handle the booking before changing it.");
        }
        validateSlot(provider.getId(), date, start, end, slotId);
        slot.setDate(date);
        slot.setStartTime(start);
        slot.setEndTime(end);
        return slotRepository.save(slot);
    }

    public void removeSlot(String email, Long slotId) {
        ServiceProvider provider = getProvider(email);
        AvailabilitySlot slot = slotRepository.findByIdAndProviderId(slotId, provider.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Slot not found"));
        if (isBooked(slot)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This slot is already booked. Handle the booking before removing it.");
        }
        slotRepository.delete(slot);
    }

    public List<AvailabilitySlot> mySlots(String email) {
        return slotRepository.findByProviderIdOrderByDateAscStartTimeAsc(getProvider(email).getId());
    }

    public List<AvailabilitySlot> slotsOfProvider(Long providerId) {
        return slotRepository.findByProviderIdOrderByDateAscStartTimeAsc(providerId);
    }

    public Map<String, Object> setCancellationPolicy(String email, int windowHours, int refundPercent) {
        if (windowHours < minWindowHours) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cancellation window must be at least " + minWindowHours + " hours.");
        }
        if (refundPercent < minRefundPercent || refundPercent > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Refund percentage must be between " + minRefundPercent + " and 100.");
        }
        ServiceProvider provider = getProvider(email);
        provider.setCancellationWindowHours(windowHours);
        provider.setLateCancellationRefundPercent(refundPercent);
        providerRepository.save(provider);
        return policyOf(provider);
    }

    public Map<String, Object> getCancellationPolicy(Long providerId) {
        ServiceProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
        return policyOf(provider);
    }

    private Map<String, Object> policyOf(ServiceProvider provider) {
        Map<String, Object> map = new HashMap<>();
        map.put("providerId", provider.getId());
        map.put("cancellationWindowHours", provider.getCancellationWindowHours());
        map.put("lateCancellationRefundPercent", provider.getLateCancellationRefundPercent());
        return map;
    }
}