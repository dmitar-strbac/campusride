package com.campusride.common.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailAlreadyExistsExceptionTest {

  @Test
  void constructor_shouldSetMessage() {
    EmailAlreadyExistsException exception = new EmailAlreadyExistsException("dmitar@test.com");

    assertThat(exception.getMessage()).isEqualTo("Email is already registered: dmitar@test.com");
  }
}
