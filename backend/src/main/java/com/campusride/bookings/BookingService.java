package com.campusride.bookings;

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
import com.campusride.notifications.NotificationType;
import com.campusride.notifications.event.NotificationEventPublisher;
import com.campusride.rides.Ride;
import com.campusride.rides.RideRepository;
import com.campusride.rides.RideStatus;
import com.campusride.users.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

  private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES =
      List.of(BookingStatus.PENDING, BookingStatus.ACCEPTED);

  private final BookingRepository bookingRepository;
  private final RideRepository rideRepository;
  private final NotificationEventPublisher notificationEvents;

  @Transactional
  public BookingResponse requestBooking(Long rideId, CreateBookingRequest request, User passenger) {

    Ride ride = rideRepository.findByIdForUpdate(rideId).orElseThrow(RideNotFoundException::new);

    validateRideCanBeBooked(ride);

    if (ride.getDriver().getId().equals(passenger.getId())) {
      throw new OwnRideBookingException();
    }

    if (request.requestedSeats() > ride.getAvailableSeats()) {
      throw new InsufficientSeatsException();
    }

    if (bookingRepository.existsByRideAndPassengerAndStatusIn(
        ride, passenger, ACTIVE_BOOKING_STATUSES)) {
      throw new DuplicateBookingException();
    }

    Booking saved =
        bookingRepository.save(
            Booking.builder()
                .ride(ride)
                .passenger(passenger)
                .requestedSeats(request.requestedSeats())
                .status(BookingStatus.PENDING)
                .build());

    notificationEvents.publish(
        ride.getDriver().getId(),
        NotificationType.BOOKING_REQUESTED,
        ride,
        saved.getId(),
        passenger,
        saved.getRequestedSeats());

    return BookingResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> getMyBookings(User passenger) {
    return bookingRepository.findByPassengerOrderByRideDepartureTimeDesc(passenger).stream()
        .map(BookingResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> getRideBookingRequests(Long rideId, User driver) {
    Ride ride = rideRepository.findById(rideId).orElseThrow(RideNotFoundException::new);

    ensureDriverOwnsRide(ride, driver);

    return bookingRepository.findByRideOrderByCreatedAtDesc(ride).stream()
        .map(BookingResponse::from)
        .toList();
  }

  @Transactional
  public BookingResponse acceptBooking(Long bookingId, User driver) {
    Booking booking = lockBookingAndRide(bookingId);
    Ride ride = booking.getRide();

    ensureDriverOwnsRide(ride, driver);
    ensureStatus(booking, BookingStatus.PENDING, "Only pending bookings can be accepted");
    validateRideCanBeBooked(ride);

    if (booking.getRequestedSeats() > ride.getAvailableSeats()) {
      throw new InsufficientSeatsException();
    }

    ride.setAvailableSeats(ride.getAvailableSeats() - booking.getRequestedSeats());
    booking.setStatus(BookingStatus.ACCEPTED);

    rideRepository.save(ride);
    Booking saved = bookingRepository.save(booking);

    notificationEvents.publish(
        saved.getPassenger().getId(),
        NotificationType.BOOKING_ACCEPTED,
        ride,
        saved.getId(),
        driver,
        saved.getRequestedSeats());

    return BookingResponse.from(saved);
  }

  @Transactional
  public BookingResponse rejectBooking(Long bookingId, User driver) {
    Booking booking = lockBookingAndRide(bookingId);

    ensureDriverOwnsRide(booking.getRide(), driver);
    ensureStatus(booking, BookingStatus.PENDING, "Only pending bookings can be rejected");
    validateRideCanBeBooked(booking.getRide());

    booking.setStatus(BookingStatus.REJECTED);
    Booking saved = bookingRepository.save(booking);

    notificationEvents.publish(
        saved.getPassenger().getId(),
        NotificationType.BOOKING_REJECTED,
        saved.getRide(),
        saved.getId(),
        driver,
        saved.getRequestedSeats());

    return BookingResponse.from(saved);
  }

  @Transactional
  public BookingResponse cancelBooking(Long bookingId, User passenger) {
    Booking booking = lockBookingAndRide(bookingId);
    Ride ride = booking.getRide();

    if (!booking.getPassenger().getId().equals(passenger.getId())) {
      throw new BookingAccessDeniedException();
    }

    if (!ride.getDepartureTime().isAfter(LocalDateTime.now())) {
      throw new InvalidBookingStateException("Bookings cannot be cancelled after departure");
    }

    if (ride.getStatus() != RideStatus.ACTIVE) {
      throw new InvalidBookingStateException("Bookings on cancelled rides cannot be cancelled");
    }

    if (booking.getStatus() != BookingStatus.PENDING
        && booking.getStatus() != BookingStatus.ACCEPTED) {
      throw new InvalidBookingStateException("Only pending or accepted bookings can be cancelled");
    }

    if (booking.getStatus() == BookingStatus.ACCEPTED) {
      ride.setAvailableSeats(ride.getAvailableSeats() + booking.getRequestedSeats());
      rideRepository.save(ride);
    }

    booking.setStatus(BookingStatus.CANCELLED);
    Booking saved = bookingRepository.save(booking);

    notificationEvents.publish(
        ride.getDriver().getId(),
        NotificationType.BOOKING_CANCELLED,
        ride,
        saved.getId(),
        passenger,
        saved.getRequestedSeats());

    return BookingResponse.from(saved);
  }

  private Booking lockBookingAndRide(Long bookingId) {
    Long rideId =
        bookingRepository
            .findRideIdByBookingId(bookingId)
            .orElseThrow(BookingNotFoundException::new);

    Ride ride = rideRepository.findByIdForUpdate(rideId).orElseThrow(RideNotFoundException::new);

    Booking booking =
        bookingRepository.findByIdForUpdate(bookingId).orElseThrow(BookingNotFoundException::new);

    booking.setRide(ride);
    return booking;
  }

  private void validateRideCanBeBooked(Ride ride) {
    if (ride.getStatus() != RideStatus.ACTIVE) {
      throw new InvalidBookingStateException("Only active rides can be booked");
    }

    if (!ride.getDepartureTime().isAfter(LocalDateTime.now())) {
      throw new InvalidBookingStateException("Past rides cannot be booked");
    }
  }

  private void ensureDriverOwnsRide(Ride ride, User driver) {
    if (!ride.getDriver().getId().equals(driver.getId())) {
      throw new RideAccessDeniedException();
    }
  }

  private void ensureStatus(Booking booking, BookingStatus expected, String message) {
    if (booking.getStatus() != expected) {
      throw new InvalidBookingStateException(message);
    }
  }
}
