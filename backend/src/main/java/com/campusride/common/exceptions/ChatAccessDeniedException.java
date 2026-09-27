package com.campusride.common.exceptions;

public class ChatAccessDeniedException extends RuntimeException {
  public ChatAccessDeniedException() {
    super("You do not have access to this ride chat");
  }
}
