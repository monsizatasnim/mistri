package com.mistry.platform.service;

import com.mistry.platform.dto.ProviderSearchResponse;
import com.mistry.platform.dto.RankedProviderResponse;
import com.mistry.platform.entity.Booking;
import com.mistry.platform.entity.PlatformSettings;
import com.mistry.platform.entity.Review;
import com.mistry.platform.repository.BookingScheduleRepository;
import com.mistry.platform.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ProviderRankingService {

    private static final double NEUTRAL = 0.5;
    private static final int DAYS_AHEAD = 7;
    private static final int BUSY_LIMIT = 10;
    private static final Set<String> NOT_BUSY = Set.of("CANCELLED", "COMPLETED", "REJECTED");

    private final ProviderSearchService providerSearchService;
    private final ReviewRepository reviewRepository;
    private final BookingScheduleRepository bookingRepository;
    private final PlatformSettingsService platformSettingsService;

    public ProviderRankingService(ProviderSearchService providerSearchService,
                                  ReviewRepository reviewRepository,
                                  BookingScheduleRepository bookingRepository,
                                  PlatformSettingsService platformSettingsService) {
        this.providerSearchService = providerSearchService;
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.platformSettingsService = platformSettingsService;
    }

    private record Facts(ProviderSearchResponse provider, Double averageRating,
                         int reviewCount, int upcomingBookings) {}

    private record Scored(Facts facts, double rating, double closeness,
                          double price, double availability, double total) {}

    public List<RankedProviderResponse> rank(String category, String location, String area,
                                             Double minPrice, Double maxPrice,
                                             String verificationStatus) {
        List<ProviderSearchResponse> found =
                providerSearchService.search(category, location, minPrice, maxPrice, verificationStatus);

        // Weights come from the admin settings, never hard-coded here.
        PlatformSettings w = platformSettingsService.getSettings();
        double wRating = w.getWeightRating().doubleValue();
        double wDistance = w.getWeightDistance().doubleValue();
        double wPrice = w.getWeightPrice().doubleValue();
        double wAvailability = w.getWeightAvailability().doubleValue();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(DAYS_AHEAD);

        List<Facts> facts = found.stream().map(p -> collectFacts(p, now, limit)).toList();

        // Price is compared with the other providers in the same results.
        List<Double> mids = facts.stream()
                .map(f -> midPrice(f.provider()))
                .filter(Objects::nonNull)
                .toList();
        double lowest = mids.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double highest = mids.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        List<Scored> scored = new ArrayList<>();
        for (Facts f : facts) {
            double rating = f.averageRating() == null ? NEUTRAL : f.averageRating() / 5.0;
            double closeness = closenessScore(area, f.provider().getServiceLocation());
            double price = priceScore(midPrice(f.provider()), lowest, highest);
            double availability = Math.max(0, 1.0 - (double) f.upcomingBookings() / BUSY_LIMIT);
            double total = (rating * wRating + closeness * wDistance
                    + price * wPrice + availability * wAvailability) * 100;
            scored.add(new Scored(f, rating, closeness, price, availability, total));
        }

        scored.sort((a, b) -> {
            int c = Double.compare(b.total(), a.total());
            return c != 0 ? c : Long.compare(a.facts().provider().getId(), b.facts().provider().getId());
        });

        List<RankedProviderResponse> result = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            Scored s = scored.get(i);
            ProviderSearchResponse p = s.facts().provider();
            result.add(new RankedProviderResponse(
                    i + 1,
                    p.getId(),
                    p.getFullName(),
                    p.getPhone(),
                    p.getServiceCategory(),
                    p.getServiceDescription(),
                    p.getServiceLocation(),
                    p.getPriceMin(),
                    p.getPriceMax(),
                    p.getPriceNote(),
                    p.getVerificationStatus(),
                    s.facts().averageRating() == null ? null : round(s.facts().averageRating(), 2),
                    s.facts().reviewCount(),
                    s.facts().upcomingBookings(),
                    round(s.total(), 1),
                    round(s.rating(), 2),
                    round(s.closeness(), 2),
                    round(s.price(), 2),
                    round(s.availability(), 2)));
        }
        return result;
    }

    private Facts collectFacts(ProviderSearchResponse p, LocalDateTime now, LocalDateTime limit) {
        List<Review> reviews = reviewRepository.findByProviderId(p.getId());
        Double average = reviews.isEmpty() ? null
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        int upcoming = 0;
        for (Booking b : bookingRepository.findByProviderId(p.getId())) {
            LocalDateTime time = b.getAppointmentTime();
            boolean inWindow = time != null && time.isAfter(now) && time.isBefore(limit);
            boolean busy = b.getStatus() == null || !NOT_BUSY.contains(b.getStatus().trim().toUpperCase());
            if (inWindow && busy) {
                upcoming++;
            }
        }
        return new Facts(p, average, reviews.size(), upcoming);
    }

    // Text-based closeness: the database has area names, not coordinates.
    private double closenessScore(String customerArea, String providerArea) {
        if (customerArea == null || customerArea.isBlank()) {
            return NEUTRAL;
        }
        if (providerArea == null || providerArea.isBlank()) {
            return 0;
        }
        String c = customerArea.trim().toLowerCase();
        String p = providerArea.trim().toLowerCase();
        if (c.equals(p)) {
            return 1;
        }
        if (p.contains(c) || c.contains(p)) {
            return 0.5;
        }
        return 0;
    }

    private Double midPrice(ProviderSearchResponse p) {
        Double low = p.getPriceMin() != null ? p.getPriceMin() : p.getPriceMax();
        Double high = p.getPriceMax() != null ? p.getPriceMax() : p.getPriceMin();
        if (low == null || high == null) {
            return null;
        }
        return (low + high) / 2.0;
    }

    // Cheapest gets 1, most expensive gets 0, no price gets the neutral score.
    private double priceScore(Double mid, double lowest, double highest) {
        if (mid == null) {
            return NEUTRAL;
        }
        if (highest <= lowest) {
            return 1;
        }
        return 1.0 - (mid - lowest) / (highest - lowest);
    }

    private double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
}
