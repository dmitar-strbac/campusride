package com.campusride.rides;

import com.campusride.rides.dto.CreateRideRequest;
import com.campusride.rides.dto.RideResponse;
import com.campusride.users.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

  private final RideService rideService;

  @PostMapping
  public RideResponse createRide(
      @Valid @RequestBody CreateRideRequest request, @AuthenticationPrincipal User user) {
    return rideService.createRide(request, user);
  }

  @GetMapping
  public List<RideResponse> searchRides(
      @RequestParam(required = false) String origin,
      @RequestParam(required = false) String destination) {
    return rideService.searchRides(origin, destination);
  }

  @GetMapping("/{id}")
  public RideResponse getRide(@PathVariable Long id) {
    return rideService.getRide(id);
  }

  @GetMapping("/my")
  public List<RideResponse> getMyOfferedRides(@AuthenticationPrincipal User user) {
    return rideService.getMyOfferedRides(user);
  }

  @PatchMapping("/{id}/cancel")
  public RideResponse cancelRide(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return rideService.cancelRide(id, user);
  }
}
