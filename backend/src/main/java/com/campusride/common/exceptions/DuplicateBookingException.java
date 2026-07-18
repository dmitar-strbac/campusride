package com.campusride.common.exceptions;

public class DuplicateBookingException extends RuntimeException {

  public DuplicateBookingException() {
    super("You already have an active booking for this ride");
  }
}
