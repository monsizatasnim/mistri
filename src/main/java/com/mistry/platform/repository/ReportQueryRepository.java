package com.mistry.platform.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only counting queries for the admin summary report. */
@Repository
public class ReportQueryRepository {

    public record RatingStats(long reviewCount, Double averageRating) {
    }

    public record ProviderRating(Long providerId, long reviewCount, double averageRating) {
    }

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Long> bookingCountsByStatus(LocalDateTime from, LocalDateTime to) {
        return grouped("select b.status, count(b) from BookingEntity b "
                + "where b.createdAt >= :rangeStart and b.createdAt < :rangeEnd group by b.status", from, to);
    }

    public RatingStats ratingStats(LocalDateTime from, LocalDateTime to) {
        Object[] row = entityManager.createQuery(
                        "select count(r), avg(r.rating) from Review r "
                                + "where r.createdAt >= :rangeStart and r.createdAt < :rangeEnd", Object[].class)
                .setParameter("rangeStart", from)
                .setParameter("rangeEnd", to)
                .getSingleResult();
        long count = ((Number) row[0]).longValue();
        Double average = (row[1] == null) ? null : ((Number) row[1]).doubleValue();
        return new RatingStats(count, average);
    }

    public List<ProviderRating> ratingsByProvider(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = entityManager.createQuery(
                        "select r.providerId, count(r), avg(r.rating) from Review r "
                                + "where r.createdAt >= :rangeStart and r.createdAt < :rangeEnd "
                                + "group by r.providerId order by avg(r.rating) desc, count(r) desc", Object[].class)
                .setParameter("rangeStart", from)
                .setParameter("rangeEnd", to)
                .getResultList();
        List<ProviderRating> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ProviderRating(((Number) row[0]).longValue(),
                    ((Number) row[1]).longValue(), ((Number) row[2]).doubleValue()));
        }
        return result;
    }

    public Map<String, Long> complaintCountsByStatus(LocalDateTime from, LocalDateTime to) {
        return grouped("select c.status, count(c) from Complaint c "
                + "where c.createdAt >= :rangeStart and c.createdAt < :rangeEnd group by c.status", from, to);
    }

    public Map<String, Long> complaintCountsByDecision(LocalDateTime from, LocalDateTime to) {
        return grouped("select c.resolutionDecision, count(c) from Complaint c "
                + "where c.createdAt >= :rangeStart and c.createdAt < :rangeEnd "
                + "and c.resolutionDecision is not null group by c.resolutionDecision", from, to);
    }

    public BigDecimal totalRefunds(LocalDateTime from, LocalDateTime to) {
        Query query = entityManager.createQuery(
                "select sum(c.refundAmount) from Complaint c "
                        + "where c.createdAt >= :rangeStart and c.createdAt < :rangeEnd");
        query.setParameter("rangeStart", from);
        query.setParameter("rangeEnd", to);
        Object result = query.getSingleResult();
        return (result == null) ? BigDecimal.ZERO : new BigDecimal(result.toString());
    }

    private Map<String, Long> grouped(String jpql, LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setParameter("rangeStart", from)
                .setParameter("rangeEnd", to)
                .getResultList();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = (row[0] == null) ? "UNKNOWN" : String.valueOf(row[0]);
            result.put(key, ((Number) row[1]).longValue());
        }
        return result;
    }
}
