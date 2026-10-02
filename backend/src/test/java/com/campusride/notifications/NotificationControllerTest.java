package com.campusride.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.notifications.dto.NotificationResponse;
import com.campusride.notifications.dto.UnreadNotificationCountResponse;
import com.campusride.users.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

  @Mock NotificationService service;
  @InjectMocks NotificationController controller;

  private final User user = User.builder().id(7L).build();

  @Test
  void listsAuthenticatedUsersNotifications() {
    Page<NotificationResponse> page = Page.empty();
    when(service.getMine(7L, 0, 20)).thenReturn(page);

    assertThat(controller.getMine(user, 0, 20)).isSameAs(page);
  }

  @Test
  void returnsAuthenticatedUsersUnreadCount() {
    when(service.unreadCount(7L)).thenReturn(new UnreadNotificationCountResponse(4));

    assertThat(controller.unreadCount(user).count()).isEqualTo(4);
  }

  @Test
  void marksNotificationReadForAuthenticatedUser() {
    controller.markRead(12L, user);
    verify(service).markRead(12L, 7L);
  }

  @Test
  void marksAllReadForAuthenticatedUser() {
    when(service.markAllRead(7L)).thenReturn(new UnreadNotificationCountResponse(0));

    assertThat(controller.markAllRead(user).count()).isZero();
    verify(service).markAllRead(7L);
  }
}
