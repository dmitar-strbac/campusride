package com.campusride.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.ExpiredJwtException;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();

    String secret =
        Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());

    ReflectionTestUtils.setField(jwtService, "secret", secret);
    ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
  }

  @Test
  void generateToken_shouldGenerateValidToken() {
    String token = jwtService.generateToken("dmitar@test.com");

    assertThat(token).isNotBlank();
    assertThat(jwtService.extractUsername(token)).isEqualTo("dmitar@test.com");
    assertThat(jwtService.isTokenValid(token, "dmitar@test.com")).isTrue();
  }

  @Test
  void isTokenValid_shouldReturnFalseWhenEmailDoesNotMatch() {
    String token = jwtService.generateToken("dmitar@test.com");

    assertThat(jwtService.isTokenValid(token, "other@test.com")).isFalse();
  }

  @Test
  void isTokenValid_shouldThrowExceptionWhenTokenIsExpired() throws InterruptedException {
    ReflectionTestUtils.setField(jwtService, "expiration", 1L);

    String token = jwtService.generateToken("dmitar@test.com");

    Thread.sleep(5);

    assertThatThrownBy(() -> jwtService.isTokenValid(token, "dmitar@test.com"))
        .isInstanceOf(ExpiredJwtException.class);
  }
}
