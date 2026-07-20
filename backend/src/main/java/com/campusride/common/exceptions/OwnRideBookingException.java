package com.campusride.common.exceptions;

public class OwnRideBookingException extends RuntimeException {

  public OwnRideBookingException() {
    super("You cannot book your own ride");
  }
}
