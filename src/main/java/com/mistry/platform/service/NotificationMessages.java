package com.mistry.platform.service;

import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.entity.ResolutionDecision;
import com.mistry.platform.entity.ShopOrderStatus;

import java.math.BigDecimal;
import java.util.Locale;

/** The words of every notification, kept in one place. */
public final class NotificationMessages {

    private static final int MAX_REASON_LENGTH = 200;

    /** A title and a message. */
    public record Text(String title, String body) {
    }

    private NotificationMessages() {
    }

    public static Text booking(Long bookingId, String status) {
        String s = (status == null) ? "" : status.trim().toUpperCase(Locale.ROOT);
        String what = switch (s) {
            case "PENDING" -> "was received and is waiting for the provider";
            case "ACCEPTED", "CONFIRMED" -> "has been confirmed";
            case "DECLINED" -> "was declined by the provider";
            case "IN_PROGRESS" -> "has started";
            case "COMPLETED" -> "is completed";
            case "CANCELLED" -> "was cancelled";
            case "CANCELLED_FULL_REFUND" -> "was cancelled and you get a full refund";
            case "RESCHEDULE_REQUESTED" -> "has a reschedule request waiting for the provider";
            case "SUGGESTED_ALTERNATIVE" -> "has a new time suggested by the provider";
            default -> s.isEmpty() ? "was updated" : "is now " + readable(s);
        };
        return new Text("Booking update", "Your booking #" + bookingId + " " + what + ".");
    }

    public static Text orderForShop(Long orderId, boolean urgent) {
        String title = urgent ? "Urgent order received" : "New order received";
        String body = "Order #" + orderId + " is waiting for your response."
                + (urgent ? " The provider marked it urgent." : "");
        return new Text(title, body);
    }

    public static Text orderForProvider(Long orderId, String shopName) {
        String shop = (shopName == null || shopName.isBlank()) ? "the shop" : shopName;
        return new Text("Order placed", "Your order #" + orderId + " was sent to " + shop + ".");
    }

    public static Text orderMarkedUrgent(Long orderId) {
        return new Text("Order marked urgent", "Order #" + orderId + " was marked urgent by the provider.");
    }

    public static Text orderCancelledForShop(Long orderId) {
        return new Text("Order cancelled", "Order #" + orderId + " was cancelled by the provider.");
    }

    public static Text orderStatus(Long orderId, ShopOrderStatus status) {
        if (status == null) {
            return new Text("Order update", "Your order #" + orderId + " was updated.");
        }
        String what = switch (status) {
            case PLACED -> "was placed";
            case ACCEPTED -> "was accepted by the shop";
            case MODIFIED -> "was changed by the shop. Please check it";
            case READY -> "is ready";
            case DECLINED -> "was declined by the shop";
            case CANCELLED -> "was cancelled";
            case PREPARING -> "is being prepared by the shop";
            case OUT_FOR_DELIVERY -> "is out for delivery";
            case COMPLETED -> "is completed";
            // any status added later still gets a readable message
            default -> "is now " + readable(status.name());
        };
        return new Text("Order update", "Your order #" + orderId + " " + what + ".");
    }

    public static Text dispute(Long complaintId, ComplaintStatus status,
                               ResolutionDecision decision, BigDecimal refund) {
        if (status == null) {
            return new Text("Complaint update", "Your complaint #" + complaintId + " was updated.");
        }
        return switch (status) {
            case SUBMITTED -> new Text("Complaint received",
                    "We received your complaint #" + complaintId + ". An admin will review it.");
            case UNDER_REVIEW -> new Text("Complaint under review",
                    "An admin has started reviewing your complaint #" + complaintId + ".");
            case RESOLVED -> {
                String outcome = (decision == null) ? "resolved" : readable(decision.name());
                String body = "Your complaint #" + complaintId + " was resolved (" + outcome + ").";
                if (refund != null && refund.signum() > 0) {
                    body += " Refund amount: BDT " + refund.stripTrailingZeros().toPlainString() + ".";
                }
                yield new Text("Complaint resolved", body);
            }
            case CLOSED -> new Text("Complaint closed", "Your complaint #" + complaintId + " was closed.");
            default -> new Text("Complaint update",
                    "Your complaint #" + complaintId + " is now " + readable(status.name()) + ".");
        };
    }

    public static Text verification(RecipientType who, boolean approved, String reason) {
        if (approved) {
            String extra = (who == RecipientType.SHOP_OWNER)
                    ? " Your shop is now visible to service providers." : "";
            return new Text("Verification approved", "Your account has been verified." + extra);
        }
        String why = (reason == null || reason.isBlank())
                ? "" : " Reason: " + limit(reason.trim(), MAX_REASON_LENGTH) + ".";
        return new Text("Verification rejected", "Your verification was not approved." + why);
    }

    static String readable(String raw) {
        return raw.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private static String limit(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }
}