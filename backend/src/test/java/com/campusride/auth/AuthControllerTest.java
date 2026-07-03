package com.campusride.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusride.auth.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
  }

  @Test
  void register_shouldReturnToken() throws Exception {
    when(authService.register(any())).thenReturn(new AuthResponse("jwt-token"));

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                                {
                                                  "firstName": "Dmitar",
                                                  "lastName": "Strbac",
                                                  "email": "dmitar@test.com",
                                                  "password": "password123"
                                                }
                                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"));

    verify(authService).register(any());
  }

  @Test
  void login_shouldReturnToken() throws Exception {
    when(authService.login(any())).thenReturn(new AuthResponse("jwt-token"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                                {
                                                  "email": "dmitar@test.com",
                                                  "password": "password123"
                                                }
                                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("jwt-token"));

    verify(authService).login(any());
  }

  @Test
  void register_shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                                {
                                                  "firstName": "",
                                                  "lastName": "",
                                                  "email": "invalid-email",
                                                  "password": "123"
                                                }
                                                """))
        .andExpect(status().isBadRequest());
  }
}
