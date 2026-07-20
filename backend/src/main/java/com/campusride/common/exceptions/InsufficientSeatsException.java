package com.campusride.common.exceptions;

public class InsufficientSeatsException extends RuntimeException {

  public InsufficientSeatsException() {
    super("Not enough seats are available");
  }
}
