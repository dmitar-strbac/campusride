package com.campusride.common.exceptions;

public class NotificationNotFoundException extends RuntimeException {
  public NotificationNotFoundException() {
    super("Notification not found");
  }
}
