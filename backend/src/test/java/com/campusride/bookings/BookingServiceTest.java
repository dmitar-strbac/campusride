package com.campusride.bookings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.bookings.dto.BookingResponse;
import com.campusride.bookings.dto.CreateBookingRequest;
import com.campusride.common.exceptions.BookingAccessDeniedException;
import com.campusride.common.exceptions.BookingNotFoundException;
import com.campusride.common.exceptions.DuplicateBookingException;
import com.campusride.common.exceptions.InsufficientSeatsException;
import com.campusride.common.exceptions.InvalidBookingStateException;
import com.campusride.common.exceptions.OwnRideBookingException;
import com.campusride.common.exceptions.RideAccessDeniedException;
import com.campusride.common.exceptions.RideNotFoundException;
import com.campusride.rides.Ride;
import com.campusride.rides.RideRepository;
import com.campusride.rides.RideStatus;
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
class BookingServiceTest {

  @Mock private BookingRepository bookingRepository;
  @Mock private RideRepository rideRepository;

  @InjectMocks private BookingService bookingService;

  @Test
  void requestBooking_shouldCreatePendingBooking() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    CreateBookingRequest request = new CreateBookingRequest(2);
    Booking saved = createBooking(10L, ride, passenger, BookingStatus.PENDING, 2);

    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
    when(bookingRepository.existsByRideAndPassengerAndStatusIn(
            any(Ride.class), any(User.class), anyList()))
        .thenReturn(false);
    when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

    BookingResponse response = bookingService.requestBooking(1L, request, passenger);

    assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
    assertThat(response.requestedSeats()).isEqualTo(2);
    verify(bookingRepository).save(any(Booking.class));
  }

  @Test
  void requestBooking_shouldRejectOwnRide() {
    User driver = createUser(1L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(() -> bookingService.requestBooking(1L, new CreateBookingRequest(1), driver))
        .isInstanceOf(OwnRideBookingException.class);
  }

  @Test
  void requestBooking_shouldRejectUnavailableRide() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.CANCELLED, 3);
    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(
            () -> bookingService.requestBooking(1L, new CreateBookingRequest(1), passenger))
        .isInstanceOf(InvalidBookingStateException.class);
  }

  @Test
  void requestBooking_shouldRejectInsufficientSeats() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 1);
    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(
            () -> bookingService.requestBooking(1L, new CreateBookingRequest(2), passenger))
        .isInstanceOf(InsufficientSeatsException.class);
  }

  @Test
  void requestBooking_shouldRejectDuplicateActiveBooking() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
    when(bookingRepository.existsByRideAndPassengerAndStatusIn(
            any(Ride.class), any(User.class), anyList()))
        .thenReturn(true);

    assertThatThrownBy(
            () -> bookingService.requestBooking(1L, new CreateBookingRequest(1), passenger))
        .isInstanceOf(DuplicateBookingException.class);
  }

  @Test
  void getMyBookings_shouldReturnPassengerBookings() {
    User passenger = createUser(2L);
    Ride ride = createRide(createUser(1L), RideStatus.ACTIVE, 3);
    Booking booking = createBooking(10L, ride, passenger, BookingStatus.PENDING, 1);
    when(bookingRepository.findByPassengerOrderByRideDepartureTimeDesc(passenger))
        .thenReturn(List.of(booking));

    assertThat(bookingService.getMyBookings(passenger)).hasSize(1);
  }

  @Test
  void getRideBookingRequests_shouldRequireRideOwner() {
    User driver = createUser(1L);
    User other = createUser(3L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(() -> bookingService.getRideBookingRequests(1L, other))
        .isInstanceOf(RideAccessDeniedException.class);
  }

  @Test
  void acceptBooking_shouldDecreaseSeatsAndAccept() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    Booking booking = createBooking(10L, ride, passenger, BookingStatus.PENDING, 2);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));
    when(rideRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ride));
    when(bookingRepository.save(booking)).thenReturn(booking);
    when(rideRepository.save(ride)).thenReturn(ride);

    BookingResponse response = bookingService.acceptBooking(10L, driver);

    assertThat(response.status()).isEqualTo(BookingStatus.ACCEPTED);
    assertThat(ride.getAvailableSeats()).isEqualTo(1);
  }

  @Test
  void acceptBooking_shouldRejectWhenSeatsAreNoLongerAvailable() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 1);
    Booking booking = createBooking(10L, ride, passenger, BookingStatus.PENDING, 2);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));
    when(rideRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(() -> bookingService.acceptBooking(10L, driver))
        .isInstanceOf(InsufficientSeatsException.class);

    verify(bookingRepository, never()).save(any());
  }

  @Test
  void rejectBooking_shouldRejectPendingBooking() {
    User driver = createUser(1L);
    Booking booking =
        createBooking(
            10L,
            createRide(driver, RideStatus.ACTIVE, 3),
            createUser(2L),
            BookingStatus.PENDING,
            1);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(booking)).thenReturn(booking);

    assertThat(bookingService.rejectBooking(10L, driver).status())
        .isEqualTo(BookingStatus.REJECTED);
  }

  @Test
  void cancelBooking_shouldRestoreSeatsForAcceptedBooking() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 1);
    Booking booking = createBooking(10L, ride, passenger, BookingStatus.ACCEPTED, 2);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));
    when(rideRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ride));
    when(rideRepository.save(ride)).thenReturn(ride);
    when(bookingRepository.save(booking)).thenReturn(booking);

    BookingResponse response = bookingService.cancelBooking(10L, passenger);

    assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
    assertThat(ride.getAvailableSeats()).isEqualTo(3);
  }

  @Test
  void cancelBooking_shouldRejectDifferentPassenger() {
    User passenger = createUser(2L);
    Booking booking =
        createBooking(
            10L,
            createRide(createUser(1L), RideStatus.ACTIVE, 3),
            passenger,
            BookingStatus.PENDING,
            1);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.cancelBooking(10L, createUser(3L)))
        .isInstanceOf(BookingAccessDeniedException.class);
  }

  @Test
  void acceptBooking_shouldThrowWhenBookingDoesNotExist() {
    when(bookingRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.acceptBooking(99L, createUser(1L)))
        .isInstanceOf(BookingNotFoundException.class);
  }

  @Test
  void requestBooking_shouldThrowWhenRideDoesNotExist() {
    when(rideRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> bookingService.requestBooking(99L, new CreateBookingRequest(1), createUser(2L)))
        .isInstanceOf(RideNotFoundException.class);
  }

  @Test
  void getRideBookingRequests_shouldReturnBookingsForRideOwner() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    Booking booking = createBooking(10L, ride, passenger, BookingStatus.PENDING, 1);

    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
    when(bookingRepository.findByRideOrderByCreatedAtDesc(ride)).thenReturn(List.of(booking));

    List<BookingResponse> result = bookingService.getRideBookingRequests(1L, driver);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).status()).isEqualTo(BookingStatus.PENDING);
    assertThat(result.get(0).requestedSeats()).isEqualTo(1);

    verify(bookingRepository).findByRideOrderByCreatedAtDesc(ride);
  }

  @Test
  void getRideBookingRequests_shouldThrowWhenRideDoesNotExist() {
    when(rideRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.getRideBookingRequests(99L, createUser(1L)))
        .isInstanceOf(RideNotFoundException.class);

    verify(bookingRepository, never()).findByRideOrderByCreatedAtDesc(any(Ride.class));
  }

  @Test
  void cancelBooking_shouldCancelPendingBookingWithoutRestoringSeats() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    Booking booking = createBooking(10L, ride, passenger, BookingStatus.PENDING, 1);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(booking)).thenReturn(booking);

    BookingResponse response = bookingService.cancelBooking(10L, passenger);

    assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
    assertThat(ride.getAvailableSeats()).isEqualTo(3);

    verify(rideRepository, never()).findByIdForUpdate(any());
    verify(rideRepository, never()).save(any());
    verify(bookingRepository).save(booking);
  }

  @Test
  void cancelBooking_shouldRejectCancellationAfterDeparture() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    ride.setDepartureTime(LocalDateTime.now().minusMinutes(1));

    Booking booking = createBooking(10L, ride, passenger, BookingStatus.PENDING, 1);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.cancelBooking(10L, passenger))
        .isInstanceOf(InvalidBookingStateException.class)
        .hasMessage("Bookings cannot be cancelled after departure");

    verify(bookingRepository, never()).save(any());
    verify(rideRepository, never()).save(any());
  }

  @Test
  void cancelBooking_shouldRejectBookingWithFinalStatus() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);

    Booking booking = createBooking(10L, ride, passenger, BookingStatus.REJECTED, 1);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.cancelBooking(10L, passenger))
        .isInstanceOf(InvalidBookingStateException.class)
        .hasMessage("Only pending or accepted bookings can be cancelled");

    verify(bookingRepository, never()).save(any());
    verify(rideRepository, never()).save(any());
  }

  @Test
  void requestBooking_shouldRejectPastRide() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);
    ride.setDepartureTime(LocalDateTime.now().minusMinutes(1));

    when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

    assertThatThrownBy(
            () -> bookingService.requestBooking(1L, new CreateBookingRequest(1), passenger))
        .isInstanceOf(InvalidBookingStateException.class)
        .hasMessage("Past rides cannot be booked");

    verify(bookingRepository, never()).save(any());
  }

  @Test
  void acceptBooking_shouldRejectBookingThatIsNotPending() {
    User driver = createUser(1L);
    User passenger = createUser(2L);
    Ride ride = createRide(driver, RideStatus.ACTIVE, 3);

    Booking booking = createBooking(10L, ride, passenger, BookingStatus.ACCEPTED, 1);

    when(bookingRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.acceptBooking(10L, driver))
        .isInstanceOf(InvalidBookingStateException.class)
        .hasMessage("Only pending bookings can be accepted");

    verify(rideRepository, never()).findByIdForUpdate(any());
    verify(bookingRepository, never()).save(any());
  }

  private User createUser(Long id) {
    return User.builder()
        .id(id)
        .firstName("User")
        .lastName(String.valueOf(id))
        .email("user" + id + "@test.com")
        .password("password")
        .role(Role.STUDENT)
        .build();
  }

  private Ride createRide(User driver, RideStatus status, int seats) {
    return Ride.builder()
        .id(1L)
        .driver(driver)
        .origin("Novi Sad")
        .destination("Belgrade")
        .departureTime(LocalDateTime.now().plusDays(1))
        .availableSeats(seats)
        .pricePerSeat(BigDecimal.valueOf(1200))
        .status(status)
        .build();
  }

  private Booking createBooking(
      Long id, Ride ride, User passenger, BookingStatus status, int requestedSeats) {
    return Booking.builder()
        .id(id)
        .ride(ride)
        .passenger(passenger)
        .requestedSeats(requestedSeats)
        .status(status)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
