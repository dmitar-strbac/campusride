package com.campusride.bookings.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(@NotNull @Min(1) @Max(8) Integer requestedSeats) {}
