package com.mistry.platform.service;

import com.mistry.platform.dto.PlatformSettingsRequest;
import com.mistry.platform.entity.PlatformSettings;
import com.mistry.platform.entity.PlatformSettingsAudit;
import com.mistry.platform.exception.SettingsValidationException;
import com.mistry.platform.repository.PlatformSettingsAuditRepository;
import com.mistry.platform.repository.PlatformSettingsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PlatformSettingsService {

    private static final int MAX_WEIGHT_DECIMALS = 4;
    private static final int MAX_FEE_DECIMALS = 2;
    private static final int MAX_CANCELLATION_WINDOW_HOURS = 720; // 30 days

    private final PlatformSettingsRepository settingsRepository;
    private final PlatformSettingsAuditRepository auditRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PlatformSettingsService(PlatformSettingsRepository settingsRepository,
                                   PlatformSettingsAuditRepository auditRepository) {
        this.settingsRepository = settingsRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Current settings. Other services (ranking, booking fee, cancellation rules)
     * should call this instead of hard-coding numbers.
     */
    @Transactional
    public PlatformSettings getSettings() {
        return settingsRepository.findById(PlatformSettings.SINGLETON_ID)
                .orElseGet(() -> settingsRepository.save(PlatformSettings.defaults()));
    }

    @Transactional(readOnly = true)
    public List<PlatformSettingsAudit> getAuditLog() {
        return auditRepository.findAllByOrderByChangedAtDescIdDesc();
    }

    @Transactional
    public PlatformSettings update(PlatformSettingsRequest r, String adminIdentifier) {
        List<String> errors = validate(r);
        if (!errors.isEmpty()) {
            throw new SettingsValidationException(errors);
        }

        PlatformSettings s = getSettings();
        String adminName = resolveAdminName(adminIdentifier);
        LocalDateTime now = LocalDateTime.now();

        List<PlatformSettingsAudit> log = new ArrayList<>();
        track(log, "weightRating", s.getWeightRating(), r.weightRating(), adminName, now);
        track(log, "weightDistance", s.getWeightDistance(), r.weightDistance(), adminName, now);
        track(log, "weightPrice", s.getWeightPrice(), r.weightPrice(), adminName, now);
        track(log, "weightAvailability", s.getWeightAvailability(), r.weightAvailability(), adminName, now);
        track(log, "bookingFeeMin", s.getBookingFeeMin(), r.bookingFeeMin(), adminName, now);
        track(log, "bookingFeeMax", s.getBookingFeeMax(), r.bookingFeeMax(), adminName, now);
        track(log, "minCancellationWindowHours", s.getMinCancellationWindowHours(),
                r.minCancellationWindowHours(), adminName, now);
        track(log, "minRefundPercentage", s.getMinRefundPercentage(), r.minRefundPercentage(), adminName, now);

        if (log.isEmpty()) {
            return s; // nothing changed, nothing to log
        }

        s.setWeightRating(r.weightRating());
        s.setWeightDistance(r.weightDistance());
        s.setWeightPrice(r.weightPrice());
        s.setWeightAvailability(r.weightAvailability());
        s.setBookingFeeMin(r.bookingFeeMin());
        s.setBookingFeeMax(r.bookingFeeMax());
        s.setMinCancellationWindowHours(r.minCancellationWindowHours());
        s.setMinRefundPercentage(r.minRefundPercentage());
        s.setUpdatedBy(adminName);
        s.setUpdatedAt(now);

        settingsRepository.save(s);
        auditRepository.saveAll(log);
        return s;
    }

    // ---------------------------------------------------------------- validation

    private List<String> validate(PlatformSettingsRequest r) {
        List<String> errors = new ArrayList<>();

        // ranking weights: each 0..1, and together exactly 1
        checkWeight(errors, "weightRating", r.weightRating());
        checkWeight(errors, "weightDistance", r.weightDistance());
        checkWeight(errors, "weightPrice", r.weightPrice());
        checkWeight(errors, "weightAvailability", r.weightAvailability());
        if (r.weightRating() != null && r.weightDistance() != null
                && r.weightPrice() != null && r.weightAvailability() != null) {
            BigDecimal sum = r.weightRating().add(r.weightDistance())
                    .add(r.weightPrice()).add(r.weightAvailability());
            if (sum.compareTo(BigDecimal.ONE) != 0) {
                errors.add("Ranking weights must add up to 1 (they currently add up to " + show(sum) + ").");
            }
        }

        // booking / convenience fee range
        checkFee(errors, "bookingFeeMin", r.bookingFeeMin());
        checkFee(errors, "bookingFeeMax", r.bookingFeeMax());
        if (r.bookingFeeMin() != null && r.bookingFeeMax() != null
                && r.bookingFeeMin().compareTo(r.bookingFeeMax()) > 0) {
            errors.add("bookingFeeMin cannot be greater than bookingFeeMax.");
        }

        // minimum cancellation terms providers must follow
        if (r.minCancellationWindowHours() == null) {
            errors.add("minCancellationWindowHours is required.");
        } else if (r.minCancellationWindowHours() < 0
                || r.minCancellationWindowHours() > MAX_CANCELLATION_WINDOW_HOURS) {
            errors.add("minCancellationWindowHours must be between 0 and "
                    + MAX_CANCELLATION_WINDOW_HOURS + " hours.");
        }
        if (r.minRefundPercentage() == null) {
            errors.add("minRefundPercentage is required.");
        } else if (r.minRefundPercentage() < 0 || r.minRefundPercentage() > 100) {
            errors.add("minRefundPercentage must be between 0 and 100.");
        }

        return errors;
    }

    private void checkWeight(List<String> errors, String name, BigDecimal v) {
        if (v == null) {
            errors.add(name + " is required.");
        } else if (v.signum() < 0 || v.compareTo(BigDecimal.ONE) > 0) {
            errors.add(name + " must be between 0 and 1.");
        } else if (v.stripTrailingZeros().scale() > MAX_WEIGHT_DECIMALS) {
            errors.add(name + " can have at most " + MAX_WEIGHT_DECIMALS + " decimal places.");
        }
    }

    private void checkFee(List<String> errors, String name, BigDecimal v) {
        if (v == null) {
            errors.add(name + " is required.");
        } else if (v.signum() < 0) {
            errors.add(name + " cannot be negative.");
        } else if (v.stripTrailingZeros().scale() > MAX_FEE_DECIMALS) {
            errors.add(name + " can have at most " + MAX_FEE_DECIMALS + " decimal places.");
        }
    }

    // ---------------------------------------------------------------- audit helpers

    private void track(List<PlatformSettingsAudit> log, String field, Object oldValue,
                       Object newValue, String adminName, LocalDateTime now) {
        if (isSame(oldValue, newValue)) {
            return;
        }
        log.add(new PlatformSettingsAudit(field, show(oldValue), show(newValue), adminName, now));
    }

    private boolean isSame(Object a, Object b) {
        if (a instanceof BigDecimal x && b instanceof BigDecimal y) {
            return x.compareTo(y) == 0;
        }
        return Objects.equals(a, b);
    }

    private String show(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal d) {
            return d.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(v);
    }

    /** Turns the logged-in identifier (email or phone) into the admin's full name for the log. */
    private String resolveAdminName(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "unknown admin";
        }
        List<String> names = entityManager
                .createQuery("select a.fullName from Admin a where a.email = :v or a.phone = :v", String.class)
                .setParameter("v", identifier)
                .setMaxResults(1)
                .getResultList();
        return names.isEmpty() ? identifier : names.get(0);
    }
}