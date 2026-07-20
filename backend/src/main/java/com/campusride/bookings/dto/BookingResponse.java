package com.campusride.bookings.dto;

import com.campusride.bookings.Booking;
import com.campusride.bookings.BookingStatus;
import com.campusride.rides.RideStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
    Long id,
    Long rideId,
    Long driverId,
    String driverName,
    Long passengerId,
    String passengerName,
    String origin,
    String destination,
    LocalDateTime departureTime,
    Integer requestedSeats,
    Integer availableSeats,
    BigDecimal pricePerSeat,
    RideStatus rideStatus,
    BookingStatus status,
    LocalDateTime createdAt) {

  public static BookingResponse from(Booking booking) {
    return new BookingResponse(
        booking.getId(),
        booking.getRide().getId(),
        booking.getRide().getDriver().getId(),
        booking.getRide().getDriver().getFirstName()
            + " "
            + booking.getRide().getDriver().getLastName(),
        booking.getPassenger().getId(),
        booking.getPassenger().getFirstName() + " " + booking.getPassenger().getLastName(),
        booking.getRide().getOrigin(),
        booking.getRide().getDestination(),
        booking.getRide().getDepartureTime(),
        booking.getRequestedSeats(),
        booking.getRide().getAvailableSeats(),
        booking.getRide().getPricePerSeat(),
        booking.getRide().getStatus(),
        booking.getStatus(),
        booking.getCreatedAt());
  }
}
