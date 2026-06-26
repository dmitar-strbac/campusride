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
}
