package com.campusride.common.exceptions;

public class RideNotFoundException extends RuntimeException {

  public RideNotFoundException() {
    super("Ride not found");
  }
}
