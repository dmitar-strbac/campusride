package com.campusride.notifications.event;

import com.campusride.notifications.NotificationType;
import com.campusride.rides.Ride;
import com.campusride.users.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;

  @Transactional(propagation = Propagation.MANDATORY)
  public void publish(
      Long recipientId,
      NotificationType type,
      Ride ride,
      Long bookingId,
      User actor,
      Integer requestedSeats) {

    NotificationEvent event =
        new NotificationEvent(
            UUID.randomUUID(),
            recipientId,
            type,
            ride.getId(),
            bookingId,
            actor == null ? null : actor.getFirstName() + " " + actor.getLastName(),
            ride.getOrigin(),
            ride.getDestination(),
            requestedSeats,
            LocalDateTime.now());

    try {
      jdbc.update(
          "INSERT INTO notification_outbox (id, payload) VALUES (?, ?)",
          event.eventId(),
          objectMapper.writeValueAsString(event));
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Cannot serialize notification event", ex);
    }
  }
}
