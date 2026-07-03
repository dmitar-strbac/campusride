package com.campusride.rides;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RideEntityTest {

  @Test
  void onCreate_shouldSetTimestampsAndActiveStatus() {
    Ride ride = new Ride();

    ride.onCreate();

    assertThat(ride.getCreatedAt()).isNotNull();
    assertThat(ride.getUpdatedAt()).isNotNull();
    assertThat(ride.getStatus()).isEqualTo(RideStatus.ACTIVE);
  }

  @Test
  void onUpdate_shouldUpdateUpdatedAt() {
    Ride ride = new Ride();
    LocalDateTime oldUpdatedAt = LocalDateTime.now().minusDays(1);
    ride.setUpdatedAt(oldUpdatedAt);

    ride.onUpdate();

    assertThat(ride.getUpdatedAt()).isAfter(oldUpdatedAt);
  }
}
