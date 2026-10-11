package com.campusride.notifications.event;

import com.campusride.notifications.Notification;
import com.campusride.notifications.NotificationRepository;
import com.campusride.notifications.dto.NotificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

  private final JdbcTemplate jdbc;
  private final NotificationRepository repository;
  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  @RabbitListener(queues = NotificationRabbitConfig.QUEUE)
  @Transactional
  public void consume(byte[] body) throws IOException {
    NotificationEvent event = objectMapper.readValue(body, NotificationEvent.class);

    List<Long> insertedIds =
        jdbc.query(
            """
                        INSERT INTO notifications (
                          event_id, recipient_id, type, ride_id, booking_id,
                          actor_name, origin, destination, requested_seats, created_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (event_id) DO NOTHING
                        RETURNING id
                        """,
            (rs, row) -> rs.getLong("id"),
            event.eventId(),
            event.recipientId(),
            event.type().name(),
            event.rideId(),
            event.bookingId(),
            event.actorName(),
            event.origin(),
            event.destination(),
            event.requestedSeats(),
            event.occurredAt());

    if (insertedIds.isEmpty()) {
      return;
    }

    Notification notification = repository.findById(insertedIds.getFirst()).orElseThrow();

    NotificationResponse response = NotificationResponse.from(notification);

    String recipientEmail = notification.getRecipient().getEmail();

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            try {
              messagingTemplate.convertAndSendToUser(
                  recipientEmail, "/queue/notifications", response);
            } catch (RuntimeException ex) {
              log.warn("WebSocket delivery failed for notification {}", response.id(), ex);
            }
          }
        });
  }
}
