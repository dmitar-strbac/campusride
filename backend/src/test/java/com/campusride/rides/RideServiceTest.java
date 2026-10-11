package com.campusride.rides;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.campusride.bookings.BookingRepository;
import com.campusride.bookings.BookingStatus;
import com.campusride.common.exceptions.RideAccessDeniedException;
import com.campusride.common.exceptions.RideAlreadyCancelledException;
import com.campusride.common.exceptions.RideNotFoundException;
import com.campusride.notifications.NotificationType;
import com.campusride.notifications.event.NotificationEventPublisher;
import com.campusride.rides.dto.CreateRideRequest;
import com.campusride.rides.dto.RideResponse;
import com.campusride.users.Role;
import com.campusride.users.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

  @Mock private RideRepository rideRepository;

  @InjectMocks private RideService rideService;
  @Mock private BookingRepository bookingRepository;
  @Mock private NotificationEventPublisher notificationEvents;

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
    Ride ride = createRide(1L, createUser(1L), RideStatus.ACTIVE);

    when(rideRepository.searchActiveRides(
            eq(RideStatus.ACTIVE),
            any(),
            eq("Novi Sad"),
            eq("Belgrade"),
            eq(1),
            eq(false),
            any(),
            any()))
        .thenReturn(List.of(ride));

    List<RideResponse> response = rideService.searchRides("Novi Sad", "Belgrade", null, 1);

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().origin()).isEqualTo("Novi Sad");
  }

  @Test
  void searchRides_shouldNormalizeBlankParamsToNull() {
    when(rideRepository.searchActiveRides(
            eq(RideStatus.ACTIVE), any(), isNull(), isNull(), eq(1), eq(false), any(), any()))
        .thenReturn(List.of());

    assertThat(rideService.searchRides(" ", "", null, 1)).isEmpty();

    verify(rideRepository)
        .searchActiveRides(
            eq(RideStatus.ACTIVE), any(), isNull(), isNull(), eq(1), eq(false), any(), any());
  }

  @Test
  void searchRides_shouldTrimParams() {
    when(rideRepository.searchActiveRides(
            eq(RideStatus.ACTIVE),
            any(),
            eq("Novi Sad"),
            eq("Belgrade"),
            eq(1),
            eq(false),
            any(),
            any()))
        .thenReturn(List.of());

    rideService.searchRides("  Novi Sad  ", "  Belgrade  ", null, 1);

    verify(rideRepository)
        .searchActiveRides(
            eq(RideStatus.ACTIVE),
            any(),
            eq("Novi Sad"),
            eq("Belgrade"),
            eq(1),
            eq(false),
            any(),
            any());
  }

  @Test
  void searchRides_shouldHandleNullSearchParams() {
    when(rideRepository.searchActiveRides(
            eq(RideStatus.ACTIVE), any(), isNull(), isNull(), eq(1), eq(false), any(), any()))
        .thenReturn(List.of());

    assertThat(rideService.searchRides(null, null, null, 1)).isEmpty();
  }

  @Test
  void searchRides_shouldApplyDateAndSeats() {
    var date = java.time.LocalDate.now().plusDays(2);

    when(rideRepository.searchActiveRides(
            eq(RideStatus.ACTIVE),
            any(),
            isNull(),
            isNull(),
            eq(3),
            eq(true),
            eq(date.atStartOfDay()),
            eq(date.plusDays(1).atStartOfDay())))
        .thenReturn(List.of());

    rideService.searchRides(null, null, date, 3);

    verify(rideRepository)
        .searchActiveRides(
            eq(RideStatus.ACTIVE),
            any(),
            isNull(),
            isNull(),
            eq(3),
            eq(true),
            eq(date.atStartOfDay()),
            eq(date.plusDays(1).atStartOfDay()));
  }

  @Test
  void searchRides_shouldRejectInvalidSeats() {
    assertThatThrownBy(() -> rideService.searchRides(null, null, null, 0))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> rideService.searchRides(null, null, null, 9))
        .isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(rideRepository);
  }

  @Test
  void searchRides_shouldRejectTooLongCities() {
    assertThatThrownBy(() -> rideService.searchRides("x".repeat(256), null, null, 1))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> rideService.searchRides(null, "x".repeat(256), null, 1))
        .isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(rideRepository);
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

    when(rideRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ride));
    when(rideRepository.save(ride)).thenReturn(ride);
    when(bookingRepository.findPassengerIdsByRideIdAndStatus(1L, BookingStatus.ACCEPTED))
        .thenReturn(Set.of(2L, 3L));

    RideResponse response = rideService.cancelRide(1L, driver);

    assertThat(response.status()).isEqualTo(RideStatus.CANCELLED);
    verify(rideRepository).save(ride);
    verify(notificationEvents)
        .publish(2L, NotificationType.RIDE_CANCELLED, ride, null, driver, null);
    verify(notificationEvents)
        .publish(3L, NotificationType.RIDE_CANCELLED, ride, null, driver, null);
  }

  @Test
  void cancelRide_shouldThrowExceptionWhenRideDoesNotExist() {
    User driver = createUser(1L);

    when(rideRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> rideService.cancelRide(99L, driver))
        .isInstanceOf(RideNotFoundException.class);

    verify(rideRepository, never()).save(any());
    verifyNoInteractions(bookingRepository, notificationEvents);
  }

  @Test
  void cancelRide_shouldThrowExceptionWhenUserIsNotDriver() {
    User driver = createUser(1L);
    User otherUser = createUser(2L);
    Ride ride = createRide(1L, driver, RideStatus.ACTIVE);

    when(rideRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(() -> rideService.cancelRide(1L, otherUser))
        .isInstanceOf(RideAccessDeniedException.class);

    verify(rideRepository, never()).save(any());
    verifyNoInteractions(bookingRepository, notificationEvents);
  }

  @Test
  void cancelRide_shouldThrowExceptionWhenRideAlreadyCancelled() {
    User driver = createUser(1L);
    Ride ride = createRide(1L, driver, RideStatus.CANCELLED);

    when(rideRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(() -> rideService.cancelRide(1L, driver))
        .isInstanceOf(RideAlreadyCancelledException.class);

    verify(rideRepository, never()).save(any());
    verifyNoInteractions(bookingRepository, notificationEvents);
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
