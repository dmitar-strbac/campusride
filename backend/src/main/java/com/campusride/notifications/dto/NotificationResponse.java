package com.campusride.notifications.dto;

import com.campusride.notifications.Notification;
import com.campusride.notifications.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    NotificationType type,
    Long rideId,
    Long bookingId,
    String actorName,
    String origin,
    String destination,
    Integer requestedSeats,
    LocalDateTime createdAt,
    LocalDateTime readAt) {

  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getRideId(),
        notification.getBookingId(),
        notification.getActorName(),
        notification.getOrigin(),
        notification.getDestination(),
        notification.getRequestedSeats(),
        notification.getCreatedAt(),
        notification.getReadAt());
  }
}
