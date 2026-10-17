package com.mistry.platform.service;

import com.mistry.platform.dto.AdminActionLogResponse;
import com.mistry.platform.dto.ReportPeriod;
import com.mistry.platform.entity.Admin;
import com.mistry.platform.entity.AdminActionCategory;
import com.mistry.platform.entity.AdminActionLog;
import com.mistry.platform.entity.PlatformSettingsAudit;
import com.mistry.platform.repository.AdminActionLogRepository;
import com.mistry.platform.repository.AdminRepository;
import com.mistry.platform.repository.PlatformSettingsAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminActionLogServiceTest {

    @Mock
    private AdminActionLogRepository logRepository;
    @Mock
    private PlatformSettingsAuditRepository settingsAuditRepository;
    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminActionLogService service;

    private final ReportPeriod october = ReportPeriod.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

    private AdminActionLog entry(AdminActionCategory category, String action, LocalDateTime at) {
        return new AdminActionLog("Test Admin", category, action, "PROVIDER", 4L, null, at);
    }

    @Test
    void logActionStoresTheAdminsFullName() {
        Admin admin = new Admin("Test Admin", "admin@test.com", "01811111111", "hash");
        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        service.logAction("admin@test.com", AdminActionCategory.VERIFICATION, "PROVIDER_APPROVED",
                "PROVIDER", 4L, null);

        ArgumentCaptor<AdminActionLog> captor = ArgumentCaptor.forClass(AdminActionLog.class);
        verify(logRepository).save(captor.capture());
        assertEquals("Test Admin", captor.getValue().getAdminName());
        assertEquals("PROVIDER_APPROVED", captor.getValue().getAction());
        assertEquals(4L, captor.getValue().getTargetId());
    }

    @Test
    void logActionFallsBackToTheLoginWhenTheAdminCannotBeFound() {
        when(adminRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        service.logAction("ghost@test.com", AdminActionCategory.SUSPENSION, "CUSTOMER_SUSPENDED",
                "CUSTOMER", 1L, null);

        ArgumentCaptor<AdminActionLog> captor = ArgumentCaptor.forClass(AdminActionLog.class);
        verify(logRepository).save(captor.capture());
        assertEquals("ghost@test.com", captor.getValue().getAdminName());
    }

    @Test
    void settingsChangesAreMergedInAndTheNewestComesFirst() {
        when(logRepository.findByPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtDesc(
                october.start(), october.endExclusive()))
                .thenReturn(List.of(entry(AdminActionCategory.VERIFICATION, "PROVIDER_APPROVED",
                        LocalDateTime.of(2026, 10, 10, 10, 0))));
        PlatformSettingsAudit inRange = new PlatformSettingsAudit("weightRating", "0.4", "0.5",
                "Test Admin", LocalDateTime.of(2026, 10, 12, 9, 0));
        PlatformSettingsAudit outOfRange = new PlatformSettingsAudit("weightPrice", "0.2", "0.3",
                "Test Admin", LocalDateTime.of(2026, 9, 1, 9, 0));
        when(settingsAuditRepository.findAllByOrderByChangedAtDescIdDesc())
                .thenReturn(List.of(inRange, outOfRange));

        List<AdminActionLogResponse> result = service.getLog(october, null);

        assertEquals(2, result.size());
        assertEquals(AdminActionCategory.SETTINGS, result.get(0).category());
        assertEquals("weightRating: 0.4 -> 0.5", result.get(0).detail());
        assertEquals("Test Admin", result.get(0).adminName());
        assertEquals("PROVIDER_APPROVED", result.get(1).action());
    }

    @Test
    void filteringBySettingsDoesNotReadTheActionLog() {
        when(settingsAuditRepository.findAllByOrderByChangedAtDescIdDesc()).thenReturn(List.of());

        service.getLog(october, AdminActionCategory.SETTINGS);

        verify(logRepository, never())
                .findByPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtDesc(any(), any());
    }

    @Test
    void filteringByVerificationLeavesOutSuspensionsAndSettings() {
        when(logRepository.findByPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtDesc(
                october.start(), october.endExclusive()))
                .thenReturn(List.of(
                        entry(AdminActionCategory.VERIFICATION, "PROVIDER_REJECTED", LocalDateTime.of(2026, 10, 3, 9, 0)),
                        entry(AdminActionCategory.SUSPENSION, "CUSTOMER_SUSPENDED", LocalDateTime.of(2026, 10, 4, 9, 0))));

        List<AdminActionLogResponse> result = service.getLog(october, AdminActionCategory.VERIFICATION);

        assertEquals(1, result.size());
        assertEquals("PROVIDER_REJECTED", result.get(0).action());
        verifyNoInteractions(settingsAuditRepository);
    }
}