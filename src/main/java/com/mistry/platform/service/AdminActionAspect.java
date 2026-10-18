package com.mistry.platform.service;

import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.entity.AdminActionCategory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Records every successful verification and suspension action done through the admin
 * controllers, together with the logged-in admin's name.
 */
@Aspect
@Component
public class AdminActionAspect {

    private static final Logger log = LoggerFactory.getLogger(AdminActionAspect.class);

    public record ActionInfo(AdminActionCategory category, String action, String targetType) {
    }

    private final AdminActionLogService logService;

    public AdminActionAspect(AdminActionLogService logService) {
        this.logService = logService;
    }

    @AfterReturning("execution(public * com.mistry.platform.controller.AdminVerificationController.*(..))"
            + " || execution(public * com.mistry.platform.controller.AdminShopOwnerVerificationController.*(..))"
            + " || execution(public * com.mistry.platform.controller.AdminAccountController.*(..))")
    public void afterAdminAction(JoinPoint joinPoint) {
        try {
            String controller = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String method = joinPoint.getSignature().getName();
            Optional<ActionInfo> info = describe(controller, method);
            if (info.isEmpty()) {
                return;
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String admin = (auth == null) ? null : auth.getName();
            Object[] args = joinPoint.getArgs();

            logService.logAction(admin, info.get().category(), info.get().action(),
                    info.get().targetType(), targetId(args), reason(args));
        } catch (RuntimeException e) {
            // the admin's action already succeeded, so a logging problem must not break it
            log.warn("Could not record admin action: {}", e.getMessage());
        }
    }

    static Optional<ActionInfo> describe(String controller, String method) {
        ActionInfo info = switch (controller + "." + method) {
            case "AdminVerificationController.approve" ->
                    new ActionInfo(AdminActionCategory.VERIFICATION, "PROVIDER_APPROVED", "PROVIDER");
            case "AdminVerificationController.reject" ->
                    new ActionInfo(AdminActionCategory.VERIFICATION, "PROVIDER_REJECTED", "PROVIDER");
            case "AdminShopOwnerVerificationController.approve" ->
                    new ActionInfo(AdminActionCategory.VERIFICATION, "SHOP_OWNER_APPROVED", "SHOP_OWNER");
            case "AdminShopOwnerVerificationController.reject" ->
                    new ActionInfo(AdminActionCategory.VERIFICATION, "SHOP_OWNER_REJECTED", "SHOP_OWNER");
            case "AdminAccountController.suspendCustomer" ->
                    new ActionInfo(AdminActionCategory.SUSPENSION, "CUSTOMER_SUSPENDED", "CUSTOMER");
            case "AdminAccountController.reactivateCustomer" ->
                    new ActionInfo(AdminActionCategory.SUSPENSION, "CUSTOMER_REACTIVATED", "CUSTOMER");
            case "AdminAccountController.suspendProvider" ->
                    new ActionInfo(AdminActionCategory.SUSPENSION, "PROVIDER_SUSPENDED", "PROVIDER");
            case "AdminAccountController.reactivateProvider" ->
                    new ActionInfo(AdminActionCategory.SUSPENSION, "PROVIDER_REACTIVATED", "PROVIDER");
            default -> null;
        };
        return Optional.ofNullable(info);
    }

    private Long targetId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long id) {
                return id;
            }
        }
        return null;
    }

    private String reason(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof VerificationDecisionRequest request) {
                return request.getReason();
            }
        }
        return null;
    }
}
