package com.campusride.users;

import com.campusride.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Current user profile")
public class UserController {

  @Operation(summary = "Get my profile")
  @GetMapping("/me")
  public UserResponse getCurrentUser(@AuthenticationPrincipal User user) {
    return UserResponse.from(user);
  }
}
