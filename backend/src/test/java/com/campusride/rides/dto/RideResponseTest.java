package com.campusride.rides.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusride.rides.Ride;
import com.campusride.rides.RideStatus;
import com.campusride.users.Role;
import com.campusride.users.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RideResponseTest {

  @Test
  void from_shouldMapRideToRideResponse() {
    User driver =
        User.builder()
            .id(1L)
            .firstName("Dmitar")
            .lastName("Strbac")
            .email("dmitar@test.com")
            .password("password")
            .role(Role.STUDENT)
            .build();

    LocalDateTime departureTime = LocalDateTime.now().plusDays(1);

    Ride ride =
        Ride.builder()
            .id(10L)
            .driver(driver)
            .origin("Novi Sad")
            .destination("Belgrade")
            .departureTime(departureTime)
            .availableSeats(3)
            .pricePerSeat(BigDecimal.valueOf(1200))
            .description("Leaving from campus")
            .status(RideStatus.ACTIVE)
            .build();

    RideResponse response = RideResponse.from(ride);

    assertThat(response.id()).isEqualTo(10L);
    assertThat(response.driverId()).isEqualTo(1L);
    assertThat(response.driverName()).isEqualTo("Dmitar Strbac");
    assertThat(response.origin()).isEqualTo("Novi Sad");
    assertThat(response.destination()).isEqualTo("Belgrade");
    assertThat(response.departureTime()).isEqualTo(departureTime);
    assertThat(response.availableSeats()).isEqualTo(3);
    assertThat(response.pricePerSeat()).isEqualByComparingTo(BigDecimal.valueOf(1200));
    assertThat(response.description()).isEqualTo("Leaving from campus");
    assertThat(response.status()).isEqualTo(RideStatus.ACTIVE);
  }
}
