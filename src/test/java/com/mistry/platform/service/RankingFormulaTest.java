package com.mistry.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mistry.platform.dto.ProviderSearchResponse;
import com.mistry.platform.dto.RankedProviderResponse;
import com.mistry.platform.entity.PlatformSettings;
import com.mistry.platform.entity.Review;
import com.mistry.platform.repository.BookingScheduleRepository;
import com.mistry.platform.repository.ReviewRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Plain unit test: database lage na, shob dependency mock kora
class RankingFormulaTest {

    private final ProviderSearchService search = mock(ProviderSearchService.class);
    private final ReviewRepository reviews = mock(ReviewRepository.class);
    private final BookingScheduleRepository bookings = mock(BookingScheduleRepository.class);
    private final PlatformSettingsService settings = mock(PlatformSettingsService.class);
    private final ObjectMapper json = new ObjectMapper();

    private final ProviderRankingService service =
            new ProviderRankingService(search, reviews, bookings, settings);

    private ProviderSearchResponse provider(long id, String area, Double min, Double max) {
        ProviderSearchResponse p = mock(ProviderSearchResponse.class);
        when(p.getId()).thenReturn(id);
        when(p.getServiceLocation()).thenReturn(area);
        when(p.getPriceMin()).thenReturn(min);
        when(p.getPriceMax()).thenReturn(max);
        return p;
    }

    private void giveRatings(long providerId, int... stars) {
        List<Review> list = new ArrayList<>();
        for (int s : stars) {
            Review r = mock(Review.class);
            when(r.getRating()).thenReturn(s);
            list.add(r);
        }
        when(reviews.findByProviderId(providerId)).thenReturn(list);
    }

    private void weights(double rating, double distance, double price, double availability) {
        PlatformSettings w = mock(PlatformSettings.class);
        when(w.getWeightRating()).thenReturn(BigDecimal.valueOf(rating));
        when(w.getWeightDistance()).thenReturn(BigDecimal.valueOf(distance));
        when(w.getWeightPrice()).thenReturn(BigDecimal.valueOf(price));
        when(w.getWeightAvailability()).thenReturn(BigDecimal.valueOf(availability));
        when(settings.getSettings()).thenReturn(w);
    }

    private void searchReturns(ProviderSearchResponse... providers) {
        when(search.search(any(), any(), any(), any(), any())).thenReturn(List.of(providers));
    }

    private List<Long> rankedIds(String area) {
        List<RankedProviderResponse> ranked = service.rank(null, null, area, null, null, null);
        List<Long> ids = new ArrayList<>();
        for (RankedProviderResponse r : ranked) {
            Map<?, ?> map = json.convertValue(r, Map.class);
            Object id = map.containsKey("providerId") ? map.get("providerId") : map.get("id");
            ids.add(((Number) id).longValue());
        }
        return ids;
    }

    @Test
    void higherRatingRanksFirst() {
        weights(1, 0, 0, 0);
        searchReturns(provider(1, "Dhanmondi", 1000.0, 1000.0), provider(2, "Dhanmondi", 1000.0, 1000.0));
        giveRatings(1, 2, 2);
        giveRatings(2, 5, 5);
        assertEquals(List.of(2L, 1L), rankedIds("Dhanmondi"));
    }

    @Test
    void cheaperProviderRanksFirst() {
        weights(0, 0, 1, 0);
        searchReturns(provider(1, "Dhanmondi", 3000.0, 3000.0), provider(2, "Dhanmondi", 500.0, 500.0));
        assertEquals(List.of(2L, 1L), rankedIds("Dhanmondi"));
    }

    @Test
    void sameAreaRanksAboveDifferentArea() {
        weights(0, 1, 0, 0);
        searchReturns(provider(1, "Mirpur", 1000.0, 1000.0), provider(2, "Dhanmondi", 1000.0, 1000.0));
        assertEquals(List.of(2L, 1L), rankedIds("Dhanmondi"));
    }

    @Test
    void providerWithoutReviewsGetsNeutralRating() {
        // 4 star = 0.8, no review = 0.5, 2 star = 0.4
        weights(1, 0, 0, 0);
        searchReturns(provider(1, "Dhanmondi", 1000.0, 1000.0),
                provider(2, "Dhanmondi", 1000.0, 1000.0),
                provider(3, "Dhanmondi", 1000.0, 1000.0));
        giveRatings(2, 4, 4);
        giveRatings(3, 2, 2);
        assertEquals(List.of(2L, 1L, 3L), rankedIds("Dhanmondi"));
    }

    @Test
    void defaultWeightsCombineAllFactors() {
        // 0.4 rating, 0.3 distance, 0.2 price, 0.1 availability
        weights(0.4, 0.3, 0.2, 0.1);
        searchReturns(provider(1, "Mirpur", 2000.0, 2000.0), provider(2, "Dhanmondi", 500.0, 500.0));
        giveRatings(1, 3, 3);
        giveRatings(2, 5, 5);
        assertEquals(List.of(2L, 1L), rankedIds("Dhanmondi"));
    }

    @Test
    void equalScoresAreOrderedByProviderId() {
        weights(0.25, 0.25, 0.25, 0.25);
        searchReturns(provider(5, "Dhanmondi", 1000.0, 1000.0), provider(2, "Dhanmondi", 1000.0, 1000.0));
        assertEquals(List.of(2L, 5L), rankedIds("Dhanmondi"));
    }
}