package com.campusride.auth;

import com.campusride.auth.dto.AuthResponse;
import com.campusride.auth.dto.LoginRequest;
import com.campusride.auth.dto.RegisterRequest;
import com.campusride.common.exceptions.EmailAlreadyExistsException;
import com.campusride.users.Role;
import com.campusride.users.User;
import com.campusride.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new EmailAlreadyExistsException(request.email());
    }

    User user =
        User.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role(Role.STUDENT)
            .build();

    userRepository.save(user);

    return new AuthResponse(jwtService.generateToken(user.getEmail()));
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password()));

    return new AuthResponse(jwtService.generateToken(request.email()));
  }
}
