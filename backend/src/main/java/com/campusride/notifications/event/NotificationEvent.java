package com.campusride.notifications.event;

import com.campusride.notifications.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationEvent(
    UUID eventId,
    Long recipientId,
    NotificationType type,
    Long rideId,
    Long bookingId,
    String actorName,
    String origin,
    String destination,
    Integer requestedSeats,
    LocalDateTime occurredAt) {}
