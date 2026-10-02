package com.campusride.notifications.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.campusride.notifications.Notification;
import com.campusride.notifications.NotificationRepository;
import com.campusride.notifications.NotificationType;
import com.campusride.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

  @Mock JdbcTemplate jdbc;
  @Mock NotificationRepository repository;
  @Mock ObjectMapper objectMapper;
  @Mock SimpMessagingTemplate messagingTemplate;
  @InjectMocks NotificationEventConsumer consumer;

  private final byte[] body = new byte[] {1};

  @BeforeEach
  void beginSynchronization() {
    TransactionSynchronizationManager.initSynchronization();
  }

  @AfterEach
  void clearSynchronization() {
    TransactionSynchronizationManager.clearSynchronization();
  }

  @Test
  void pushesOnlyAfterCommit() throws Exception {
    stubNewNotification();

    consumer.consume(body);

    verifyNoInteractions(messagingTemplate);

    TransactionSynchronizationManager.getSynchronizations()
        .forEach(synchronization -> synchronization.afterCommit());

    verify(messagingTemplate)
        .convertAndSendToUser(
            eq("user@example.com"), eq("/queue/notifications"), any(Object.class));
  }

  @Test
  void ignoresRedeliveryOfExistingEvent() throws Exception {
    stubEvent();
    when(jdbc.query(
            anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any(), any(Object[].class)))
        .thenReturn(List.of());

    consumer.consume(body);

    verifyNoInteractions(repository, messagingTemplate);
  }

  @Test
  void rejectsMalformedPayloadBeforeDatabaseWrite() throws Exception {
    when(objectMapper.readValue(body, NotificationEvent.class))
        .thenThrow(new IOException("Invalid JSON"));

    assertThatThrownBy(() -> consumer.consume(body)).isInstanceOf(IOException.class);

    verifyNoInteractions(jdbc, repository, messagingTemplate);
  }

  @Test
  void websocketFailureDoesNotEscapeAfterCommit() throws Exception {
    stubNewNotification();
    doThrow(new IllegalStateException("Disconnected"))
        .when(messagingTemplate)
        .convertAndSendToUser(anyString(), anyString(), any(Object.class));

    consumer.consume(body);

    TransactionSynchronizationManager.getSynchronizations()
        .forEach(synchronization -> synchronization.afterCommit());
  }

  private void stubEvent() throws Exception {
    when(objectMapper.readValue(body, NotificationEvent.class))
        .thenReturn(
            new NotificationEvent(
                UUID.randomUUID(),
                7L,
                NotificationType.BOOKING_ACCEPTED,
                3L,
                8L,
                "Driver",
                "Novi Sad",
                "Belgrade",
                1,
                LocalDateTime.now()));
  }

  private void stubNewNotification() throws Exception {
    stubEvent();
    when(jdbc.query(
            anyString(), org.mockito.ArgumentMatchers.<RowMapper<Long>>any(), any(Object[].class)))
        .thenReturn(List.of(10L));

    Notification notification = new Notification();
    notification.setId(10L);
    notification.setRecipient(User.builder().email("user@example.com").build());
    notification.setType(NotificationType.BOOKING_ACCEPTED);
    notification.setRideId(3L);
    notification.setCreatedAt(LocalDateTime.now());

    when(repository.findById(10L)).thenReturn(Optional.of(notification));
  }
}
