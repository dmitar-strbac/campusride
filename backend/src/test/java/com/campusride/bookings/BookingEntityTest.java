package com.campusride.bookings;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BookingEntityTest {

  @Test
  void onCreate_shouldSetTimestampsAndPendingStatus() {
    Booking booking = new Booking();

    booking.onCreate();

    assertThat(booking.getCreatedAt()).isNotNull();
    assertThat(booking.getUpdatedAt()).isNotNull();
    assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
  }

  @Test
  void onCreate_shouldKeepExplicitStatus() {
    Booking booking = Booking.builder().status(BookingStatus.ACCEPTED).build();

    booking.onCreate();

    assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
  }

  @Test
  void onUpdate_shouldUpdateTimestamp() {
    Booking booking = new Booking();
    LocalDateTime oldUpdatedAt = LocalDateTime.now().minusDays(1);
    booking.setUpdatedAt(oldUpdatedAt);

    booking.onUpdate();

    assertThat(booking.getUpdatedAt()).isAfter(oldUpdatedAt);
  }
}
