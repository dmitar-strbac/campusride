package com.campusride.config;

import com.campusride.auth.JwtService;
import com.campusride.users.User;
import com.campusride.users.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    if (header == null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!header.startsWith("Bearer ") || header.substring(7).isBlank()) {
      reject(response);
      return;
    }

    try {
      String token = header.substring(7);
      String email = jwtService.extractUsername(token);

      if (email == null || email.isBlank()) {
        reject(response);
        return;
      }

      User user = userRepository.findByEmail(email).orElse(null);

      if (user == null || !jwtService.isTokenValid(token, user.getEmail())) {
        reject(response);
        return;
      }

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        var authentication =
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (JwtException | IllegalArgumentException ex) {
      reject(response);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void reject(HttpServletResponse response) throws IOException {
    SecurityContextHolder.clearContext();
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid bearer token");
  }
}
