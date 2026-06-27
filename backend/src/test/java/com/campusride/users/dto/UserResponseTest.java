package com.campusride.users.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusride.users.Role;
import com.campusride.users.User;
import org.junit.jupiter.api.Test;

class UserResponseTest {

  @Test
  void from_shouldMapUserToUserResponse() {
    User user =
        User.builder()
            .id(1L)
            .firstName("Dmitar")
            .lastName("Strbac")
            .email("dmitar@test.com")
            .password("encodedPassword")
            .role(Role.STUDENT)
            .build();

    UserResponse response = UserResponse.from(user);

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.firstName()).isEqualTo("Dmitar");
    assertThat(response.lastName()).isEqualTo("Strbac");
    assertThat(response.email()).isEqualTo("dmitar@test.com");
    assertThat(response.role()).isEqualTo(Role.STUDENT);
  }
}
