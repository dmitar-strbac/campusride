package com.campusride.bookings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.bookings.dto.BookingResponse;
import com.campusride.bookings.dto.CreateBookingRequest;
import com.campusride.rides.RideStatus;
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
class BookingControllerTest {

  @Mock private BookingService bookingService;

  @InjectMocks private BookingController bookingController;

  @Test
  void requestBooking_shouldReturnCreatedBooking() {
    User passenger = createUser(2L);
    CreateBookingRequest request = new CreateBookingRequest(2);
    BookingResponse expected = createResponse();

    when(bookingService.requestBooking(1L, request, passenger)).thenReturn(expected);

    BookingResponse response = bookingController.requestBooking(1L, request, passenger);

    assertThat(response).isEqualTo(expected);
    verify(bookingService).requestBooking(1L, request, passenger);
  }

  @Test
  void getMyBookings_shouldReturnPassengerBookings() {
    User passenger = createUser(2L);
    BookingResponse expected = createResponse();

    when(bookingService.getMyBookings(passenger)).thenReturn(List.of(expected));

    assertThat(bookingController.getMyBookings(passenger)).containsExactly(expected);
  }

  @Test
  void getRideBookingRequests_shouldReturnRideRequests() {
    User driver = createUser(1L);
    BookingResponse expected = createResponse();

    when(bookingService.getRideBookingRequests(1L, driver)).thenReturn(List.of(expected));

    assertThat(bookingController.getRideBookingRequests(1L, driver)).containsExactly(expected);
  }

  @Test
  void acceptBooking_shouldAcceptBooking() {
    User driver = createUser(1L);
    BookingResponse expected = createResponse();

    when(bookingService.acceptBooking(10L, driver)).thenReturn(expected);

    assertThat(bookingController.acceptBooking(10L, driver)).isEqualTo(expected);
  }

  @Test
  void rejectBooking_shouldRejectBooking() {
    User driver = createUser(1L);
    BookingResponse expected = createResponse();

    when(bookingService.rejectBooking(10L, driver)).thenReturn(expected);

    assertThat(bookingController.rejectBooking(10L, driver)).isEqualTo(expected);
  }

  @Test
  void cancelBooking_shouldCancelBooking() {
    User passenger = createUser(2L);
    BookingResponse expected = createResponse();

    when(bookingService.cancelBooking(10L, passenger)).thenReturn(expected);

    assertThat(bookingController.cancelBooking(10L, passenger)).isEqualTo(expected);
  }

  private User createUser(Long id) {
    return User.builder()
        .id(id)
        .firstName("Test")
        .lastName("User")
        .email("user" + id + "@test.com")
        .password("password")
        .role(Role.STUDENT)
        .build();
  }

  private BookingResponse createResponse() {
    return new BookingResponse(
        10L,
        1L,
        1L,
        "Driver User",
        2L,
        "Passenger User",
        "Novi Sad",
        "Belgrade",
        LocalDateTime.now().plusDays(1),
        2,
        3,
        BigDecimal.valueOf(1200),
        RideStatus.ACTIVE,
        BookingStatus.PENDING,
        LocalDateTime.now());
  }
}
