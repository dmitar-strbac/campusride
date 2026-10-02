package com.campusride.rides;

import com.campusride.bookings.BookingRepository;
import com.campusride.bookings.BookingStatus;
import com.campusride.common.exceptions.RideAccessDeniedException;
import com.campusride.common.exceptions.RideAlreadyCancelledException;
import com.campusride.common.exceptions.RideNotFoundException;
import com.campusride.notifications.NotificationType;
import com.campusride.notifications.event.NotificationEventPublisher;
import com.campusride.rides.dto.CreateRideRequest;
import com.campusride.rides.dto.RideResponse;
import com.campusride.users.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RideService {

  private final RideRepository rideRepository;
  private final BookingRepository bookingRepository;
  private final NotificationEventPublisher notificationEvents;

  @Transactional
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

  @Transactional(readOnly = true)
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

  @Transactional(readOnly = true)
  public RideResponse getRide(Long id) {
    return RideResponse.from(rideRepository.findById(id).orElseThrow(RideNotFoundException::new));
  }

  @Transactional(readOnly = true)
  public List<RideResponse> getMyOfferedRides(User driver) {
    return rideRepository.findByDriverOrderByDepartureTimeDesc(driver).stream()
        .map(RideResponse::from)
        .toList();
  }

  @Transactional
  public RideResponse cancelRide(Long id, User driver) {
    Ride ride = rideRepository.findByIdForUpdate(id).orElseThrow(RideNotFoundException::new);

    if (!ride.getDriver().getId().equals(driver.getId())) {
      throw new RideAccessDeniedException();
    }

    if (ride.getStatus() == RideStatus.CANCELLED) {
      throw new RideAlreadyCancelledException();
    }

    ride.setStatus(RideStatus.CANCELLED);

    RideResponse response = RideResponse.from(rideRepository.save(ride));

    for (Long passengerId :
        bookingRepository.findPassengerIdsByRideIdAndStatus(ride.getId(), BookingStatus.ACCEPTED)) {
      notificationEvents.publish(
          passengerId, NotificationType.RIDE_CANCELLED, ride, null, driver, null);
    }

    return response;
  }
}
