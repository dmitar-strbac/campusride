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

  @Transactional
  public BookingResponse requestBooking(Long rideId, CreateBookingRequest request, User passenger) {
    Ride ride = rideRepository.findById(rideId).orElseThrow(RideNotFoundException::new);

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

    Booking booking =
        Booking.builder()
            .ride(ride)
            .passenger(passenger)
            .requestedSeats(request.requestedSeats())
            .status(BookingStatus.PENDING)
            .build();

    return BookingResponse.from(bookingRepository.save(booking));
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
    Booking booking =
        bookingRepository.findByIdForUpdate(bookingId).orElseThrow(BookingNotFoundException::new);

    ensureDriverOwnsRide(booking.getRide(), driver);
    ensureStatus(booking, BookingStatus.PENDING, "Only pending bookings can be accepted");

    Ride ride =
        rideRepository
            .findByIdForUpdate(booking.getRide().getId())
            .orElseThrow(RideNotFoundException::new);

    validateRideCanBeBooked(ride);

    if (booking.getRequestedSeats() > ride.getAvailableSeats()) {
      throw new InsufficientSeatsException();
    }

    ride.setAvailableSeats(ride.getAvailableSeats() - booking.getRequestedSeats());
    booking.setRide(ride);
    booking.setStatus(BookingStatus.ACCEPTED);

    rideRepository.save(ride);
    return BookingResponse.from(bookingRepository.save(booking));
  }

  @Transactional
  public BookingResponse rejectBooking(Long bookingId, User driver) {
    Booking booking =
        bookingRepository.findByIdForUpdate(bookingId).orElseThrow(BookingNotFoundException::new);

    ensureDriverOwnsRide(booking.getRide(), driver);
    ensureStatus(booking, BookingStatus.PENDING, "Only pending bookings can be rejected");

    booking.setStatus(BookingStatus.REJECTED);

    return BookingResponse.from(bookingRepository.save(booking));
  }

  @Transactional
  public BookingResponse cancelBooking(Long bookingId, User passenger) {
    Booking booking =
        bookingRepository.findByIdForUpdate(bookingId).orElseThrow(BookingNotFoundException::new);

    if (!booking.getPassenger().getId().equals(passenger.getId())) {
      throw new BookingAccessDeniedException();
    }

    if (!booking.getRide().getDepartureTime().isAfter(LocalDateTime.now())) {
      throw new InvalidBookingStateException("Bookings cannot be cancelled after departure");
    }

    if (booking.getStatus() != BookingStatus.PENDING
        && booking.getStatus() != BookingStatus.ACCEPTED) {
      throw new InvalidBookingStateException("Only pending or accepted bookings can be cancelled");
    }

    if (booking.getStatus() == BookingStatus.ACCEPTED) {
      Ride ride =
          rideRepository
              .findByIdForUpdate(booking.getRide().getId())
              .orElseThrow(RideNotFoundException::new);

      ride.setAvailableSeats(ride.getAvailableSeats() + booking.getRequestedSeats());
      booking.setRide(ride);
      rideRepository.save(ride);
    }

    booking.setStatus(BookingStatus.CANCELLED);

    return BookingResponse.from(bookingRepository.save(booking));
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
