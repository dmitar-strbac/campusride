package com.campusride.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

public record RegisterRequest(
    @NotBlank @Size(max = 255) String firstName,
    @NotBlank @Size(max = 255) String lastName,
    @Email @NotBlank @Size(max = 255) String email,
    @NotBlank @Size(min = 6, max = 72) String password) {

  @JsonIgnore
  @AssertTrue(message = "password must not exceed 72 UTF-8 bytes")
  public boolean isPasswordWithinBcryptLimit() {
    return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
  }
}
