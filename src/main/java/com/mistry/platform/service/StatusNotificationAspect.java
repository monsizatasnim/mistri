package com.mistry.platform.service;

import com.mistry.platform.dto.ComplaintResponse;
import com.mistry.platform.dto.ShopOrderResponse;
import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.entity.RecipientType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sends a notification after a booking, order, complaint or verification service has done
 * its work. The services themselves are not changed. The aspect runs after the services'
 * own transactions have finished, and a notification problem never breaks the action.
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class StatusNotificationAspect {

    private static final Logger log = LoggerFactory.getLogger(StatusNotificationAspect.class);

    private final StatusNotifier notifier;
    private final RecipientDirectory directory;

    public StatusNotificationAspect(StatusNotifier notifier, RecipientDirectory directory) {
        this.notifier = notifier;
        this.directory = directory;
    }

    // ------------------------------------------------------------ bookings

    // every BookingService method that returns a booking changed that booking
    @AfterReturning(
            pointcut = "execution(public com.mistry.platform.entity.BookingEntity com.mistry.platform.service.BookingService.*(..))",
            returning = "booking")
    public void afterBookingChange(BookingEntity booking) {
        if (booking == null) {
            return;
        }
        run("booking", () -> notifier.bookingChanged(booking.getCustomerId(), booking.getId(), booking.getStatus()));
    }

    // ------------------------------------------------------------ disputes

    @AfterReturning(
            pointcut = "execution(public com.mistry.platform.dto.ComplaintResponse com.mistry.platform.service.ComplaintService.submit(..))"
                    + " || execution(public com.mistry.platform.dto.ComplaintResponse com.mistry.platform.service.ComplaintService.startReview(..))"
                    + " || execution(public com.mistry.platform.dto.ComplaintResponse com.mistry.platform.service.ComplaintService.resolve(..))",
            returning = "complaint")
    public void afterDisputeChange(ComplaintResponse complaint) {
        if (complaint == null) {
            return;
        }
        run("dispute", () -> notifier.disputeChanged(complaint.getCustomerId(), complaint.getId(),
                complaint.getStatus(), complaint.getResolutionDecision(), complaint.getRefundAmount()));
    }

    // ------------------------------------------------------------ verification

    @AfterReturning("execution(public * com.mistry.platform.service.VerificationService.approve(..))")
    public void afterProviderApproved(JoinPoint joinPoint) {
        verification(RecipientType.PROVIDER, joinPoint, true);
    }

    @AfterReturning("execution(public * com.mistry.platform.service.VerificationService.reject(..))")
    public void afterProviderRejected(JoinPoint joinPoint) {
        verification(RecipientType.PROVIDER, joinPoint, false);
    }

    @AfterReturning("execution(public * com.mistry.platform.service.ShopOwnerVerificationService.approve(..))")
    public void afterShopOwnerApproved(JoinPoint joinPoint) {
        verification(RecipientType.SHOP_OWNER, joinPoint, true);
    }

    @AfterReturning("execution(public * com.mistry.platform.service.ShopOwnerVerificationService.reject(..))")
    public void afterShopOwnerRejected(JoinPoint joinPoint) {
        verification(RecipientType.SHOP_OWNER, joinPoint, false);
    }

    private void verification(RecipientType who, JoinPoint joinPoint, boolean approved) {
        run("verification", () -> {
            Object[] args = joinPoint.getArgs();
            if (args.length == 0 || !(args[0] instanceof Long id)) {
                return;
            }
            String reason = null;
            for (Object arg : args) {
                if (arg instanceof VerificationDecisionRequest request) {
                    reason = request.getReason();
                }
            }
            notifier.verificationDecided(who, id, approved, reason);
        });
    }

    // ------------------------------------------------------------ orders

    @AfterReturning(
            pointcut = "execution(public com.mistry.platform.dto.ShopOrderResponse com.mistry.platform.service.ShopOrderService.placeOrder(..))",
            returning = "order")
    public void afterOrderPlaced(JoinPoint joinPoint, ShopOrderResponse order) {
        if (order == null) {
            return;
        }
        run("order", () -> {
            // the first argument of placeOrder is the provider's login
            Long providerId = null;
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof String login) {
                providerId = directory.idOf(RecipientType.PROVIDER, login).orElse(null);
            }
            notifier.orderPlaced(providerId, order.shopOwnerId(), order.id(), order.shopName(), order.urgent());
        });
    }

    @AfterReturning(
            pointcut = "execution(public com.mistry.platform.dto.ShopOrderResponse com.mistry.platform.service.ShopOrderService.markUrgent(..))",
            returning = "order")
    public void afterOrderMarkedUrgent(ShopOrderResponse order) {
        if (order == null) {
            return;
        }
        run("order", () -> notifier.orderMarkedUrgent(order.shopOwnerId(), order.id()));
    }

    // ------------------------------------------------------------

    private void run(String what, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            // the booking, order, complaint or verification already went through
            log.warn("Could not send the {} notification: {}", what, e.getMessage());
        }
    }
}
