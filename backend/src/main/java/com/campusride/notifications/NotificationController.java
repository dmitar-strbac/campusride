package com.campusride.notifications;

import com.campusride.notifications.dto.NotificationResponse;
import com.campusride.notifications.dto.UnreadNotificationCountResponse;
import com.campusride.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "My notifications and read status")
public class NotificationController {

  private final NotificationService service;

  @Operation(summary = "Get my notifications")
  @GetMapping
  public Page<NotificationResponse> getMine(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.getMine(user.getId(), page, size);
  }

  @Operation(summary = "Get my unread notification count")
  @GetMapping("/unread-count")
  public UnreadNotificationCountResponse unreadCount(@AuthenticationPrincipal User user) {
    return service.unreadCount(user.getId());
  }

  @Operation(summary = "Mark one notification as read")
  @PatchMapping("/{id}/read")
  public NotificationResponse markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return service.markRead(id, user.getId());
  }

  @Operation(summary = "Mark all my notifications as read")
  @PatchMapping("/read-all")
  public UnreadNotificationCountResponse markAllRead(@AuthenticationPrincipal User user) {
    return service.markAllRead(user.getId());
  }
}
