package com.campusride.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.campusride.common.exceptions.NotificationNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock NotificationRepository repository;
  @InjectMocks NotificationService service;

  @Test
  void listsOnlyCurrentUsersNotifications() {
    when(repository.findByRecipientIdOrderByCreatedAtDescIdDesc(7L, PageRequest.of(1, 10)))
        .thenReturn(new PageImpl<>(java.util.List.of(notification())));

    var page = service.getMine(7L, 1, 10);

    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().getFirst().type()).isEqualTo(NotificationType.BOOKING_ACCEPTED);
  }

  @Test
  void rejectsInvalidPaginationBeforeQueryingDatabase() {
    assertThatThrownBy(() -> service.getMine(7L, -1, 20))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> service.getMine(7L, 0, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> service.getMine(7L, 0, 51))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(repository);
  }

  @Test
  void countsOnlyCurrentUsersUnreadNotifications() {
    when(repository.countByRecipientIdAndReadAtIsNull(7L)).thenReturn(3L);

    assertThat(service.unreadCount(7L).count()).isEqualTo(3);
  }

  @Test
  void marksOwnedNotificationRead() {
    Notification notification = notification();
    when(repository.findByIdAndRecipientId(12L, 7L)).thenReturn(Optional.of(notification));

    var response = service.markRead(12L, 7L);

    assertThat(response.readAt()).isNotNull();
    assertThat(notification.getReadAt()).isNotNull();
  }

  @Test
  void markingAlreadyReadNotificationDoesNotReplaceTimestamp() {
    Notification notification = notification();
    LocalDateTime original = LocalDateTime.of(2026, 9, 1, 10, 0);
    notification.setReadAt(original);
    when(repository.findByIdAndRecipientId(12L, 7L)).thenReturn(Optional.of(notification));

    assertThat(service.markRead(12L, 7L).readAt()).isEqualTo(original);
  }

  @Test
  void doesNotRevealAnotherUsersNotification() {
    when(repository.findByIdAndRecipientId(12L, 7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.markRead(12L, 7L))
        .isInstanceOf(NotificationNotFoundException.class);
  }

  @Test
  void marksOnlyCurrentUsersNotificationsRead() {
    assertThat(service.markAllRead(7L).count()).isZero();
    verify(repository).markAllRead(7L);
  }

  private Notification notification() {
    Notification notification = new Notification();
    notification.setId(12L);
    notification.setType(NotificationType.BOOKING_ACCEPTED);
    notification.setRideId(2L);
    notification.setOrigin("Novi Sad");
    notification.setDestination("Belgrade");
    notification.setCreatedAt(LocalDateTime.now());
    return notification;
  }
}
