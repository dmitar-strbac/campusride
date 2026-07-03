package com.campusride.rides;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.common.exceptions.RideAccessDeniedException;
import com.campusride.common.exceptions.RideAlreadyCancelledException;
import com.campusride.common.exceptions.RideNotFoundException;
import com.campusride.rides.dto.CreateRideRequest;
import com.campusride.rides.dto.RideResponse;
import com.campusride.users.Role;
import com.campusride.users.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

  @Mock private RideRepository rideRepository;

  @InjectMocks private RideService rideService;

  @Test
  void createRide_shouldCreateRideAndReturnResponse() {
    User driver = createUser(1L);
    CreateRideRequest request =
        new CreateRideRequest(
            "Novi Sad",
            "Belgrade",
            LocalDateTime.now().plusDays(1),
            3,
            BigDecimal.valueOf(1200),
            "Leaving from campus");

    Ride savedRide =
        Ride.builder()
            .id(10L)
            .driver(driver)
            .origin(request.origin())
            .destination(request.destination())
            .departureTime(request.departureTime())
            .availableSeats(request.availableSeats())
            .pricePerSeat(request.pricePerSeat())
            .description(request.description())
            .status(RideStatus.ACTIVE)
            .build();

    when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);

    RideResponse response = rideService.createRide(request, driver);

    assertThat(response.id()).isEqualTo(10L);
    assertThat(response.driverId()).isEqualTo(1L);
    assertThat(response.origin()).isEqualTo("Novi Sad");
    assertThat(response.destination()).isEqualTo("Belgrade");
    assertThat(response.availableSeats()).isEqualTo(3);
    assertThat(response.pricePerSeat()).isEqualByComparingTo(BigDecimal.valueOf(1200));
    assertThat(response.status()).isEqualTo(RideStatus.ACTIVE);

    verify(rideRepository).save(any(Ride.class));
  }

  @Test
  void searchRides_shouldReturnActiveFutureRides() {
    User driver = createUser(1L);
    Ride ride = createRide(1L, driver, RideStatus.ACTIVE);

    when(rideRepository.searchActiveRides(
            eq(RideStatus.ACTIVE), any(), eq("Novi Sad"), eq("Belgrade")))
        .thenReturn(List.of(ride));

    List<RideResponse> response = rideService.searchRides("Novi Sad", "Belgrade");

    assertThat(response).hasSize(1);
    assertThat(response.get(0).origin()).isEqualTo("Novi Sad");
    assertThat(response.get(0).destination()).isEqualTo("Belgrade");
  }

  @Test
  void searchRides_shouldNormalizeBlankParamsToNull() {
    when(rideRepository.searchActiveRides(eq(RideStatus.ACTIVE), any(), isNull(), isNull()))
        .thenReturn(List.of());

    List<RideResponse> response = rideService.searchRides(" ", "");

    assertThat(response).isEmpty();
    verify(rideRepository).searchActiveRides(eq(RideStatus.ACTIVE), any(), isNull(), isNull());
  }

  @Test
  void searchRides_shouldTrimParams() {
    when(rideRepository.searchActiveRides(
            eq(RideStatus.ACTIVE), any(), eq("Novi Sad"), eq("Belgrade")))
        .thenReturn(List.of());

    rideService.searchRides("  Novi Sad  ", "  Belgrade  ");

    verify(rideRepository)
        .searchActiveRides(eq(RideStatus.ACTIVE), any(), eq("Novi Sad"), eq("Belgrade"));
  }

  @Test
  void searchRides_shouldHandleNullSearchParams() {
    when(rideRepository.searchActiveRides(eq(RideStatus.ACTIVE), any(), isNull(), isNull()))
        .thenReturn(List.of());

    rideService.searchRides(null, null);

    verify(rideRepository).searchActiveRides(eq(RideStatus.ACTIVE), any(), isNull(), isNull());
  }

  @Test
  void getRide_shouldReturnRideWhenExists() {
    User driver = createUser(1L);
    Ride ride = createRide(5L, driver, RideStatus.ACTIVE);

    when(rideRepository.findById(5L)).thenReturn(Optional.of(ride));

    RideResponse response = rideService.getRide(5L);

    assertThat(response.id()).isEqualTo(5L);
    assertThat(response.driverId()).isEqualTo(1L);
  }

  @Test
  void getRide_shouldThrowExceptionWhenRideDoesNotExist() {
    when(rideRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rideService.getRide(99L)).isInstanceOf(RideNotFoundException.class);
  }

  @Test
  void getMyOfferedRides_shouldReturnDriverRides() {
    User driver = createUser(1L);
    Ride ride = createRide(1L, driver, RideStatus.ACTIVE);

    when(rideRepository.findByDriverOrderByDepartureTimeDesc(driver)).thenReturn(List.of(ride));

    List<RideResponse> response = rideService.getMyOfferedRides(driver);

    assertThat(response).hasSize(1);
    assertThat(response.get(0).driverId()).isEqualTo(1L);
  }

  @Test
  void cancelRide_shouldCancelRideWhenDriverOwnsRide() {
    User driver = createUser(1L);
    Ride ride = createRide(1L, driver, RideStatus.ACTIVE);

    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
    when(rideRepository.save(ride)).thenReturn(ride);

    RideResponse response = rideService.cancelRide(1L, driver);

    assertThat(response.status()).isEqualTo(RideStatus.CANCELLED);
    verify(rideRepository).save(ride);
  }

  @Test
  void cancelRide_shouldThrowExceptionWhenRideDoesNotExist() {
    User driver = createUser(1L);

    when(rideRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rideService.cancelRide(99L, driver))
        .isInstanceOf(RideNotFoundException.class);

    verify(rideRepository, never()).save(any());
  }

  @Test
  void cancelRide_shouldThrowExceptionWhenUserIsNotDriver() {
    User driver = createUser(1L);
    User otherUser = createUser(2L);
    Ride ride = createRide(1L, driver, RideStatus.ACTIVE);

    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(() -> rideService.cancelRide(1L, otherUser))
        .isInstanceOf(RideAccessDeniedException.class);

    verify(rideRepository, never()).save(any());
  }

  @Test
  void cancelRide_shouldThrowExceptionWhenRideAlreadyCancelled() {
    User driver = createUser(1L);
    Ride ride = createRide(1L, driver, RideStatus.CANCELLED);

    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(() -> rideService.cancelRide(1L, driver))
        .isInstanceOf(RideAlreadyCancelledException.class);

    verify(rideRepository, never()).save(any());
  }

  private User createUser(Long id) {
    return User.builder()
        .id(id)
        .firstName("Dmitar")
        .lastName("Strbac")
        .email("dmitar" + id + "@test.com")
        .password("password")
        .role(Role.STUDENT)
        .build();
  }

  private Ride createRide(Long id, User driver, RideStatus status) {
    return Ride.builder()
        .id(id)
        .driver(driver)
        .origin("Novi Sad")
        .destination("Belgrade")
        .departureTime(LocalDateTime.now().plusDays(1))
        .availableSeats(3)
        .pricePerSeat(BigDecimal.valueOf(1200))
        .description("Leaving from campus")
        .status(status)
        .build();
  }
}
