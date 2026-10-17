package com.mistry.platform.service;

import com.mistry.platform.controller.AdminVerificationController;
import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.entity.AdminActionCategory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminActionAspectTest {

    @Mock
    private AdminActionLogService logService;

    private AdminActionAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new AdminActionAspect(logService);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of()));
    }

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    private JoinPoint joinPoint(String method, Object... args) {
        JoinPoint joinPoint = mock(JoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(AdminVerificationController.class);
        when(signature.getName()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    @Test
    void verificationActionsAreRecognised() {
        assertEquals("PROVIDER_APPROVED",
                AdminActionAspect.describe("AdminVerificationController", "approve").get().action());
        assertEquals("PROVIDER_REJECTED",
                AdminActionAspect.describe("AdminVerificationController", "reject").get().action());
        assertEquals("SHOP_OWNER_APPROVED",
                AdminActionAspect.describe("AdminShopOwnerVerificationController", "approve").get().action());
        assertEquals("SHOP_OWNER_REJECTED",
                AdminActionAspect.describe("AdminShopOwnerVerificationController", "reject").get().action());
    }

    @Test
    void suspensionActionsAreRecognised() {
        assertEquals("CUSTOMER_SUSPENDED",
                AdminActionAspect.describe("AdminAccountController", "suspendCustomer").get().action());
        assertEquals("CUSTOMER_REACTIVATED",
                AdminActionAspect.describe("AdminAccountController", "reactivateCustomer").get().action());
        assertEquals("PROVIDER_SUSPENDED",
                AdminActionAspect.describe("AdminAccountController", "suspendProvider").get().action());
        assertEquals("PROVIDER_REACTIVATED",
                AdminActionAspect.describe("AdminAccountController", "reactivateProvider").get().action());
        assertEquals(AdminActionCategory.SUSPENSION,
                AdminActionAspect.describe("AdminAccountController", "suspendProvider").get().category());
    }

    @Test
    void readOnlyMethodsAreNotLogged() {
        assertTrue(AdminActionAspect.describe("AdminVerificationController", "getPending").isEmpty());
        assertTrue(AdminActionAspect.describe("AdminAccountController", "searchCustomers").isEmpty());
    }

    @Test
    void aRejectionIsLoggedWithTheAdminTheTargetAndTheReason() {
        VerificationDecisionRequest request = mock(VerificationDecisionRequest.class);
        when(request.getReason()).thenReturn("Blurry NID");

        aspect.afterAdminAction(joinPoint("reject", 5L, request));

        verify(logService).logAction("admin@test.com", AdminActionCategory.VERIFICATION,
                "PROVIDER_REJECTED", "PROVIDER", 5L, "Blurry NID");
    }

    @Test
    void aFailureWhileLoggingNeverBreaksTheAdminsAction() {
        doThrow(new RuntimeException("db down")).when(logService)
                .logAction(any(), any(), any(), any(), any(), any());

        assertDoesNotThrow(() -> aspect.afterAdminAction(joinPoint("approve", 5L)));
    }
}