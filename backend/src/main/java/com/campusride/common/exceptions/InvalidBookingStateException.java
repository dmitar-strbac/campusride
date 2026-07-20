package com.campusride.common.exceptions;

public class InvalidBookingStateException extends RuntimeException {

  public InvalidBookingStateException(String message) {
    super(message);
  }
}
