package com.campusride.rides.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateRideRequest(
    @NotBlank String origin,
    @NotBlank String destination,
    @NotNull @Future LocalDateTime departureTime,
    @NotNull @Min(1) @Max(8) Integer availableSeats,
    @NotNull @DecimalMin("0.0") BigDecimal pricePerSeat,
    String description) {}
