package dev.solace.twiggle.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.config.TestSecurityConfig;
import dev.solace.twiggle.dto.AuthDTO;
import dev.solace.twiggle.dto.AuthResponse;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.service.AuthService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({RateLimiterConfiguration.class, AuthControllerTest.AuthTestConfig.class, TestSecurityConfig.class})
class AuthControllerTest {

    @TestConfiguration
    static class AuthTestConfig {
        @Bean
        @Primary
        public AuthService authService() {
            return org.mockito.Mockito.mock(AuthService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthDTO validAuthDTO;
    private AuthResponse validAuthResponse;

    @BeforeEach
    void setUp() {
        validAuthDTO = new AuthDTO("test@example.com", "password123");
        validAuthResponse = new AuthResponse("jwt-token", "test@example.com", UUID.randomUUID(), List.of("USER"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        AuthDTO validAuthDTO = new AuthDTO("test@example.com", "password123");
        AuthResponse validAuthResponse = new AuthResponse(
                "valid-jwt-token",
                "test@example.com",
                UUID.fromString("2e57afc9-6e8b-4cc3-9b6d-7d672e10ab92"),
                List.of("authenticated"));
        when(authService.loginUser(validAuthDTO.getEmail(), validAuthDTO.getPassword()))
                .thenReturn(ResponseEntity.ok(validAuthResponse));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", validAuthDTO.getEmail())
                        .param("password", validAuthDTO.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(validAuthResponse.getAccess_token()))
                .andExpect(jsonPath("$.email").value(validAuthResponse.getEmail()))
                .andExpect(jsonPath("$.roles").isArray());

        assertEquals("test@example.com", validAuthResponse.getEmail());
        assertEquals(UUID.fromString("2e57afc9-6e8b-4cc3-9b6d-7d672e10ab92"), validAuthResponse.getUserId());
        assertNotNull(validAuthResponse.getAccess_token());
        assertTrue(validAuthResponse.getRoles().contains("authenticated"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        AuthDTO invalidAuthDTO = new AuthDTO("wrong@example.com", "wrongpassword");
        when(authService.loginUser(invalidAuthDTO.getEmail(), invalidAuthDTO.getPassword()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", invalidAuthDTO.getEmail())
                        .param("password", invalidAuthDTO.getPassword()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_WithValidData_ShouldReturnOk() throws Exception {
        doNothing().when(authService).registerUser(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Registration successful"));
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        AuthDTO invalidEmailDTO = new AuthDTO("invalid-email", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithInvalidPassword_ShouldReturnBadRequest() throws Exception {
        AuthDTO invalidPasswordDTO = new AuthDTO("test@example.com", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPasswordDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        doThrow(new CustomException("Email already exists", HttpStatus.BAD_REQUEST))
                .when(authService)
                .registerUser(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthDTO)))
                .andExpect(status().isBadRequest());
    }
}
