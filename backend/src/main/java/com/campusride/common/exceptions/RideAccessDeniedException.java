package com.campusride.common.exceptions;

public class RideAccessDeniedException extends RuntimeException {

  public RideAccessDeniedException() {
    super("You are not allowed to modify this ride");
  }
}
