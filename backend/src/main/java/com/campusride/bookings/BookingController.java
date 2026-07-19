package com.campusride.bookings;

import com.campusride.bookings.dto.BookingResponse;
import com.campusride.bookings.dto.CreateBookingRequest;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;

  @PostMapping("/rides/{rideId}/bookings")
  public BookingResponse requestBooking(
      @PathVariable Long rideId,
      @Valid @RequestBody CreateBookingRequest request,
      @AuthenticationPrincipal User user) {
    return bookingService.requestBooking(rideId, request, user);
  }

  @GetMapping("/bookings/my")
  public List<BookingResponse> getMyBookings(@AuthenticationPrincipal User user) {
    return bookingService.getMyBookings(user);
  }

  @GetMapping("/rides/{rideId}/bookings")
  public List<BookingResponse> getRideBookingRequests(
      @PathVariable Long rideId, @AuthenticationPrincipal User user) {
    return bookingService.getRideBookingRequests(rideId, user);
  }

  @PatchMapping("/bookings/{bookingId}/accept")
  public BookingResponse acceptBooking(
      @PathVariable Long bookingId, @AuthenticationPrincipal User user) {
    return bookingService.acceptBooking(bookingId, user);
  }

  @PatchMapping("/bookings/{bookingId}/reject")
  public BookingResponse rejectBooking(
      @PathVariable Long bookingId, @AuthenticationPrincipal User user) {
    return bookingService.rejectBooking(bookingId, user);
  }

  @PatchMapping("/bookings/{bookingId}/cancel")
  public BookingResponse cancelBooking(
      @PathVariable Long bookingId, @AuthenticationPrincipal User user) {
    return bookingService.cancelBooking(bookingId, user);
  }
}
