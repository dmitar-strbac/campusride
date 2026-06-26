package com.campusride.common.exceptions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusride.auth.AuthController;
import com.campusride.auth.AuthService;
import com.campusride.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class GlobalExceptionHandlerTest {

  private AuthService authService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    authService = Mockito.mock(AuthService.class);

    mockMvc =
        MockMvcBuilders.standaloneSetup(new AuthController(authService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void handleEmailAlreadyExists_shouldReturnConflict() throws Exception {
    Mockito.when(authService.register(Mockito.any(RegisterRequest.class)))
        .thenThrow(new EmailAlreadyExistsException("dmitar@test.com"));

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
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value("Email is already registered: dmitar@test.com"))
        .andExpect(jsonPath("$.path").value("/api/auth/register"));
  }

  @Test
  void handleValidation_shouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                                {
                                                  "firstName": "",
                                                  "lastName": "Strbac",
                                                  "email": "invalid-email",
                                                  "password": "123"
                                                }
                                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"));
  }
}
