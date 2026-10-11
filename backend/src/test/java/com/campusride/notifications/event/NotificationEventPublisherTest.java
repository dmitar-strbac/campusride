package com.campusride.notifications.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.campusride.notifications.NotificationType;
import com.campusride.rides.Ride;
import com.campusride.users.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

  @Mock JdbcTemplate jdbc;
  @Mock ObjectMapper objectMapper;
  @InjectMocks NotificationEventPublisher publisher;

  @Test
  void snapshotsBookingInformationIntoOutbox() throws Exception {
    Ride ride = ride();
    User actor = User.builder().firstName("Dmitar").lastName("Strbac").build();
    when(objectMapper.writeValueAsString(any(NotificationEvent.class)))
        .thenReturn("{\"event\":true}");

    publisher.publish(7L, NotificationType.BOOKING_REQUESTED, ride, 8L, actor, 2);

    var captor = ArgumentCaptor.forClass(NotificationEvent.class);
    verify(objectMapper).writeValueAsString(captor.capture());
    NotificationEvent event = captor.getValue();

    assertThat(event.eventId()).isNotNull();
    assertThat(event.recipientId()).isEqualTo(7L);
    assertThat(event.rideId()).isEqualTo(3L);
    assertThat(event.bookingId()).isEqualTo(8L);
    assertThat(event.actorName()).isEqualTo("Dmitar Strbac");
    assertThat(event.requestedSeats()).isEqualTo(2);
    assertThat(event.occurredAt()).isNotNull();

    verify(jdbc)
        .update(
            "INSERT INTO notification_outbox (id, payload) VALUES (?, ?)",
            event.eventId(),
            "{\"event\":true}");
  }

  @Test
  void supportsRideEventWithoutActorOrBooking() throws Exception {
    when(objectMapper.writeValueAsString(any(NotificationEvent.class))).thenReturn("{}");

    publisher.publish(7L, NotificationType.RIDE_CANCELLED, ride(), null, null, null);

    var captor = ArgumentCaptor.forClass(NotificationEvent.class);
    verify(objectMapper).writeValueAsString(captor.capture());
    assertThat(captor.getValue().actorName()).isNull();
    assertThat(captor.getValue().bookingId()).isNull();
  }

  @Test
  void failsBeforeDatabaseWriteWhenSerializationFails() throws Exception {
    when(objectMapper.writeValueAsString(any(NotificationEvent.class)))
        .thenThrow(new JsonProcessingException("Serialization failed") {});

    assertThatThrownBy(
            () -> publisher.publish(7L, NotificationType.RIDE_CANCELLED, ride(), null, null, null))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(jdbc);
  }

  private Ride ride() {
    return Ride.builder().id(3L).origin("Novi Sad").destination("Belgrade").build();
  }
}
