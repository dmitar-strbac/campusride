package com.campusride.rides;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.rides.dto.CreateRideRequest;
import com.campusride.rides.dto.RideResponse;
import com.campusride.users.Role;
import com.campusride.users.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RideControllerTest {

  @Mock private RideService rideService;

  @InjectMocks private RideController rideController;

  @Test
  void createRide_shouldCreateRide() {
    User user = createUser();
    CreateRideRequest request =
        new CreateRideRequest(
            "Novi Sad",
            "Belgrade",
            LocalDateTime.now().plusDays(1),
            3,
            BigDecimal.valueOf(1200),
            "Leaving from campus");

    RideResponse expectedResponse = createRideResponse();

    when(rideService.createRide(request, user)).thenReturn(expectedResponse);

    RideResponse response = rideController.createRide(request, user);

    assertThat(response).isEqualTo(expectedResponse);
    verify(rideService).createRide(request, user);
  }

  @Test
  void searchRides_shouldReturnRides() {
    RideResponse expectedResponse = createRideResponse();

    when(rideService.searchRides("Novi Sad", "Belgrade")).thenReturn(List.of(expectedResponse));

    List<RideResponse> response = rideController.searchRides("Novi Sad", "Belgrade");

    assertThat(response).containsExactly(expectedResponse);
    verify(rideService).searchRides("Novi Sad", "Belgrade");
  }

  @Test
  void getRide_shouldReturnRide() {
    RideResponse expectedResponse = createRideResponse();

    when(rideService.getRide(1L)).thenReturn(expectedResponse);

    RideResponse response = rideController.getRide(1L);

    assertThat(response).isEqualTo(expectedResponse);
    verify(rideService).getRide(1L);
  }

  @Test
  void getMyOfferedRides_shouldReturnCurrentUserRides() {
    User user = createUser();
    RideResponse expectedResponse = createRideResponse();

    when(rideService.getMyOfferedRides(user)).thenReturn(List.of(expectedResponse));

    List<RideResponse> response = rideController.getMyOfferedRides(user);

    assertThat(response).containsExactly(expectedResponse);
    verify(rideService).getMyOfferedRides(user);
  }

  @Test
  void cancelRide_shouldCancelRide() {
    User user = createUser();
    RideResponse expectedResponse = createRideResponse();

    when(rideService.cancelRide(1L, user)).thenReturn(expectedResponse);

    RideResponse response = rideController.cancelRide(1L, user);

    assertThat(response).isEqualTo(expectedResponse);
    verify(rideService).cancelRide(1L, user);
  }

  private User createUser() {
    return User.builder()
        .id(1L)
        .firstName("Dmitar")
        .lastName("Strbac")
        .email("dmitar@test.com")
        .password("password")
        .role(Role.STUDENT)
        .build();
  }

  private RideResponse createRideResponse() {
    return new RideResponse(
        1L,
        1L,
        "Dmitar Strbac",
        "Novi Sad",
        "Belgrade",
        LocalDateTime.now().plusDays(1),
        3,
        BigDecimal.valueOf(1200),
        "Leaving from campus",
        RideStatus.ACTIVE);
  }
}
