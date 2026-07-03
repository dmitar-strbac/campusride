package com.campusride.users.dto;

import com.campusride.users.Role;
import com.campusride.users.User;

public record UserResponse(Long id, String firstName, String lastName, String email, Role role) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getRole());
  }
}
