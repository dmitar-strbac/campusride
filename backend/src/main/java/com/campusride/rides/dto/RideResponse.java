package com.campusride.rides.dto;

import com.campusride.rides.Ride;
import com.campusride.rides.RideStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(
    Long id,
    Long driverId,
    String driverName,
    String origin,
    String destination,
    LocalDateTime departureTime,
    Integer availableSeats,
    BigDecimal pricePerSeat,
    String description,
    RideStatus status) {

  public static RideResponse from(Ride ride) {
    return new RideResponse(
        ride.getId(),
        ride.getDriver().getId(),
        ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName(),
        ride.getOrigin(),
        ride.getDestination(),
        ride.getDepartureTime(),
        ride.getAvailableSeats(),
        ride.getPricePerSeat(),
        ride.getDescription(),
        ride.getStatus());
  }
}
