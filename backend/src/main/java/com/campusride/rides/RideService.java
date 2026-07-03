package com.campusride.rides;

import com.campusride.common.exceptions.RideAccessDeniedException;
import com.campusride.common.exceptions.RideAlreadyCancelledException;
import com.campusride.common.exceptions.RideNotFoundException;
import com.campusride.rides.dto.CreateRideRequest;
import com.campusride.rides.dto.RideResponse;
import com.campusride.users.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideService {

  private final RideRepository rideRepository;

  public RideResponse createRide(CreateRideRequest request, User driver) {
    Ride ride =
        Ride.builder()
            .driver(driver)
            .origin(request.origin())
            .destination(request.destination())
            .departureTime(request.departureTime())
            .availableSeats(request.availableSeats())
            .pricePerSeat(request.pricePerSeat())
            .description(request.description())
            .build();

    return RideResponse.from(rideRepository.save(ride));
  }

  public List<RideResponse> searchRides(String origin, String destination) {
    return rideRepository
        .searchActiveRides(
            RideStatus.ACTIVE,
            LocalDateTime.now(),
            normalizeSearchParam(origin),
            normalizeSearchParam(destination))
        .stream()
        .map(RideResponse::from)
        .toList();
  }

  private String normalizeSearchParam(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  public RideResponse getRide(Long id) {
    return RideResponse.from(rideRepository.findById(id).orElseThrow(RideNotFoundException::new));
  }

  public List<RideResponse> getMyOfferedRides(User driver) {
    return rideRepository.findByDriverOrderByDepartureTimeDesc(driver).stream()
        .map(RideResponse::from)
        .toList();
  }

  public RideResponse cancelRide(Long id, User driver) {
    Ride ride = rideRepository.findById(id).orElseThrow(RideNotFoundException::new);

    if (!ride.getDriver().getId().equals(driver.getId())) {
      throw new RideAccessDeniedException();
    }

    if (ride.getStatus() == RideStatus.CANCELLED) {
      throw new RideAlreadyCancelledException();
    }

    ride.setStatus(RideStatus.CANCELLED);

    return RideResponse.from(rideRepository.save(ride));
  }
}
