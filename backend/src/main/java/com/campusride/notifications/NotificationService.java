package com.campusride.notifications;

import com.campusride.common.exceptions.NotificationNotFoundException;
import com.campusride.notifications.dto.NotificationResponse;
import com.campusride.notifications.dto.UnreadNotificationCountResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository repository;

  @Transactional(readOnly = true)
  public Page<NotificationResponse> getMine(Long userId, int page, int size) {
    if (page < 0 || size < 1 || size > 50) {
      throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and 50");
    }

    return repository
        .findByRecipientIdOrderByCreatedAtDescIdDesc(userId, PageRequest.of(page, size))
        .map(NotificationResponse::from);
  }

  @Transactional(readOnly = true)
  public UnreadNotificationCountResponse unreadCount(Long userId) {
    return new UnreadNotificationCountResponse(
        repository.countByRecipientIdAndReadAtIsNull(userId));
  }

  @Transactional
  public NotificationResponse markRead(Long notificationId, Long userId) {
    Notification notification =
        repository
            .findByIdAndRecipientId(notificationId, userId)
            .orElseThrow(() -> new NotificationNotFoundException());

    if (notification.getReadAt() == null) {
      notification.setReadAt(LocalDateTime.now());
    }

    return NotificationResponse.from(notification);
  }

  @Transactional
  public UnreadNotificationCountResponse markAllRead(Long userId) {
    repository.markAllRead(userId);
    return new UnreadNotificationCountResponse(0);
  }
}
