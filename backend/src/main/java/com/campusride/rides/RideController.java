package com.campusride.rides;

import com.campusride.rides.dto.CreateRideRequest;
import com.campusride.rides.dto.RideResponse;
import com.campusride.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Rides", description = "Publish, search, and manage rides")
public class RideController {

  private final RideService rideService;

  @Operation(summary = "Publish a ride")
  @PostMapping
  public RideResponse createRide(
      @Valid @RequestBody CreateRideRequest request, @AuthenticationPrincipal User user) {
    return rideService.createRide(request, user);
  }

  @Operation(summary = "Search availablerides")
  @GetMapping
  public List<RideResponse> searchRides(
      @RequestParam(required = false) String origin,
      @RequestParam(required = false) String destination) {
    return rideService.searchRides(origin, destination);
  }

  @Operation(summary = "Get ride details")
  @GetMapping("/{id}")
  public RideResponse getRide(@PathVariable Long id) {
    return rideService.getRide(id);
  }

  @Operation(summary = "Get rides I published")
  @GetMapping("/my")
  public List<RideResponse> getMyOfferedRides(@AuthenticationPrincipal User user) {
    return rideService.getMyOfferedRides(user);
  }

  @Operation(summary = "Cancel a ride")
  @PatchMapping("/{id}/cancel")
  public RideResponse cancelRide(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return rideService.cancelRide(id, user);
  }
}
