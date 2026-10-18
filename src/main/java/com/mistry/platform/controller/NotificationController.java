package com.mistry.platform.controller;

import com.mistry.platform.dto.NotificationResponse;
import com.mistry.platform.exception.NotificationException;
import com.mistry.platform.service.NotificationRecipient;
import com.mistry.platform.service.NotificationService;
import com.mistry.platform.service.RecipientDirectory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** The logged-in customer's, provider's or shop owner's own notifications. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final RecipientDirectory recipientDirectory;

    public NotificationController(NotificationService notificationService,
                                  RecipientDirectory recipientDirectory) {
        this.notificationService = notificationService;
        this.recipientDirectory = recipientDirectory;
    }

    @GetMapping
    public List<NotificationResponse> list(Authentication authentication,
                                           @RequestParam(defaultValue = "false") boolean unreadOnly) {
        NotificationRecipient me = recipientDirectory.currentRecipient(authentication);
        return notificationService.list(me, unreadOnly).stream().map(NotificationResponse::from).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        NotificationRecipient me = recipientDirectory.currentRecipient(authentication);
        return Map.of("unread", notificationService.unreadCount(me));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(Authentication authentication, @PathVariable Long id) {
        NotificationRecipient me = recipientDirectory.currentRecipient(authentication);
        return NotificationResponse.from(notificationService.markRead(me, id));
    }

    @PostMapping("/read-all")
    public Map<String, Integer> markAllRead(Authentication authentication) {
        NotificationRecipient me = recipientDirectory.currentRecipient(authentication);
        return Map.of("marked", notificationService.markAllRead(me));
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<Map<String, String>> handle(NotificationException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
