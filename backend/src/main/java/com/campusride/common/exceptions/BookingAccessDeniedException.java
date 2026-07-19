package com.campusride.common.exceptions;

public class BookingAccessDeniedException extends RuntimeException {

  public BookingAccessDeniedException() {
    super("You are not allowed to modify this booking");
  }
}
