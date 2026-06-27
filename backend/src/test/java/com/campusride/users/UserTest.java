package com.campusride.users;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void getAuthorities_shouldReturnRoleAuthority() {
    User user = User.builder().email("dmitar@test.com").role(Role.STUDENT).build();

    assertThat(user.getAuthorities()).hasSize(1);

    assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
  }

  @Test
  void getUsername_shouldReturnEmail() {
    User user = User.builder().email("dmitar@test.com").build();

    assertThat(user.getUsername()).isEqualTo("dmitar@test.com");
  }
}
