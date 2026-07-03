package com.campusride.users;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusride.users.dto.UserResponse;
import org.junit.jupiter.api.Test;

class UserControllerTest {

  private final UserController userController = new UserController();

  @Test
  void getCurrentUser_shouldReturnAuthenticatedUser() {
    User user =
        User.builder()
            .id(1L)
            .firstName("Dmitar")
            .lastName("Strbac")
            .email("dmitar@test.com")
            .password("encodedPassword")
            .role(Role.STUDENT)
            .build();

    UserResponse response = userController.getCurrentUser(user);

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.firstName()).isEqualTo("Dmitar");
    assertThat(response.lastName()).isEqualTo("Strbac");
    assertThat(response.email()).isEqualTo("dmitar@test.com");
    assertThat(response.role()).isEqualTo(Role.STUDENT);
  }
}
