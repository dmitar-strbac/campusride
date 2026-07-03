package com.campusride.common.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RideExceptionsTest {

  @Test
  void rideNotFoundException_shouldSetMessage() {
    assertThat(new RideNotFoundException().getMessage()).isEqualTo("Ride not found");
  }

  @Test
  void rideAccessDeniedException_shouldSetMessage() {
    assertThat(new RideAccessDeniedException().getMessage())
        .isEqualTo("You are not allowed to modify this ride");
  }

  @Test
  void rideAlreadyCancelledException_shouldSetMessage() {
    assertThat(new RideAlreadyCancelledException().getMessage())
        .isEqualTo("Ride is already cancelled");
  }
}
