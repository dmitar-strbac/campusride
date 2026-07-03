package com.campusride.users;

import com.campusride.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  @GetMapping("/me")
  public UserResponse getCurrentUser(@AuthenticationPrincipal User user) {
    return UserResponse.from(user);
  }
}
