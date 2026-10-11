package com.campusride.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.auth.dto.AuthResponse;
import com.campusride.auth.dto.LoginRequest;
import com.campusride.auth.dto.RegisterRequest;
import com.campusride.common.exceptions.EmailAlreadyExistsException;
import com.campusride.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthService authService;

  @Test
  void register_shouldCreateUserAndReturnToken() {
    RegisterRequest request =
        new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", "password123");

    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
    when(jwtService.generateToken(request.email())).thenReturn("jwt-token");

    AuthResponse response = authService.register(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    verify(userRepository).save(any());
  }

  @Test
  void register_shouldThrowExceptionWhenEmailAlreadyExists() {
    RegisterRequest request =
        new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", "password123");

    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(EmailAlreadyExistsException.class)
        .hasMessageContaining("Email is already registered");

    verify(userRepository, never()).save(any());
  }

  @Test
  void login_shouldAuthenticateUserAndReturnToken() {
    LoginRequest request = new LoginRequest("dmitar@test.com", "password123");

    when(jwtService.generateToken(request.email())).thenReturn("jwt-token");

    AuthResponse response = authService.login(request);

    assertThat(response.token()).isEqualTo("jwt-token");
    verify(authenticationManager).authenticate(any());
  }

  @Test
  void register_shouldMapConcurrentEmailConflictTo409Exception() {
    RegisterRequest request =
        new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", "password123");

    var sql = new java.sql.SQLException("duplicate email", "23505");

    var violation =
        new org.hibernate.exception.ConstraintViolationException(
            "duplicate email", sql, "users_email_key");

    when(userRepository.save(any()))
        .thenThrow(
            new org.springframework.dao.DataIntegrityViolationException(
                "duplicate email", violation));

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(EmailAlreadyExistsException.class);

    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void register_shouldNotHideOtherDatabaseErrors() {
    RegisterRequest request =
        new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", "password123");

    var error = new org.springframework.dao.DataIntegrityViolationException("another constraint");

    when(userRepository.save(any())).thenThrow(error);

    assertThatThrownBy(() -> authService.register(request)).isSameAs(error);
  }

  @Test
  void registerRequest_shouldRejectMissingPassword() {
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var violations =
          factory
              .getValidator()
              .validate(new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", null));

      assertThat(violations)
          .extracting(item -> item.getPropertyPath().toString())
          .contains("password");
    }
  }

  @Test
  void registerRequest_shouldRejectPasswordExceedingBcryptByteLimit() {
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var violations =
          factory
              .getValidator()
              .validate(new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", "č".repeat(37)));

      assertThat(violations)
          .extracting(item -> item.getPropertyPath().toString())
          .contains("passwordWithinBcryptLimit");
    }
  }

  @Test
  void register_shouldPropagateConstraintViolationWithDifferentSqlState() {
    RegisterRequest request =
        new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", "password123");

    var sqlException = new java.sql.SQLException("not-null constraint violation", "23502");

    var constraintViolation =
        new org.hibernate.exception.ConstraintViolationException(
            "not-null constraint violation", sqlException, "users_email_key");

    var databaseError =
        new org.springframework.dao.DataIntegrityViolationException(
            "registration failed", constraintViolation);

    when(userRepository.save(any())).thenThrow(databaseError);

    assertThatThrownBy(() -> authService.register(request)).isSameAs(databaseError);

    verify(jwtService, never()).generateToken(any());
  }

  @Test
  void register_shouldPropagateUniqueViolationForDifferentConstraint() {
    RegisterRequest request =
        new RegisterRequest("Dmitar", "Strbac", "dmitar@test.com", "password123");

    var sqlException = new java.sql.SQLException("different unique constraint violation", "23505");

    var constraintViolation =
        new org.hibernate.exception.ConstraintViolationException(
            "different unique constraint violation", sqlException, "users_other_unique_key");

    var databaseError =
        new org.springframework.dao.DataIntegrityViolationException(
            "registration failed", constraintViolation);

    when(userRepository.save(any())).thenThrow(databaseError);

    assertThatThrownBy(() -> authService.register(request)).isSameAs(databaseError);

    verify(jwtService, never()).generateToken(any());
  }
}
