package com.campusride.common.exceptions;

public class RideAlreadyCancelledException extends RuntimeException {

  public RideAlreadyCancelledException() {
    super("Ride is already cancelled");
  }
}
