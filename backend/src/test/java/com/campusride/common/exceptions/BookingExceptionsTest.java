package com.campusride.common.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BookingExceptionsTest {

  @Test
  void bookingNotFoundException_shouldSetMessage() {
    assertThat(new BookingNotFoundException().getMessage()).isEqualTo("Booking not found");
  }

  @Test
  void bookingAccessDeniedException_shouldSetMessage() {
    assertThat(new BookingAccessDeniedException().getMessage())
        .isEqualTo("You are not allowed to modify this booking");
  }

  @Test
  void duplicateBookingException_shouldSetMessage() {
    assertThat(new DuplicateBookingException().getMessage())
        .isEqualTo("You already have an active booking for this ride");
  }

  @Test
  void insufficientSeatsException_shouldSetMessage() {
    assertThat(new InsufficientSeatsException().getMessage())
        .isEqualTo("Not enough seats are available");
  }

  @Test
  void ownRideBookingException_shouldSetMessage() {
    assertThat(new OwnRideBookingException().getMessage())
        .isEqualTo("You cannot book your own ride");
  }

  @Test
  void invalidBookingStateException_shouldSetCustomMessage() {
    assertThat(new InvalidBookingStateException("Custom message").getMessage())
        .isEqualTo("Custom message");
  }
}
