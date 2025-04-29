package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.AuthResponse;
import dev.solace.twiggle.model.AuthUser;
import dev.solace.twiggle.repository.AuthUserRepository;
import dev.solace.twiggle.security.JwtUtils;
import dev.solace.twiggle.supabase.SupabaseAdminClient;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SupabaseAdminClient supabaseAdminClient;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private UserLoadingService userLoadingService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final String JWT_TOKEN = "jwt.token.here";
    private static final UUID USER_ID = UUID.randomUUID();

    private UserDetails userDetails;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        userDetails = new User(
                TEST_EMAIL, ENCODED_PASSWORD, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        authUser = new AuthUser();
        authUser.setId(USER_ID);
        authUser.setEmail(TEST_EMAIL);
        authUser.setRole("ROLE_USER");
    }

    @Test
    void registerUser_Success() {
        // Arrange
        doNothing().when(supabaseAdminClient).createUser(TEST_EMAIL, TEST_PASSWORD);

        // Act
        authService.registerUser(TEST_EMAIL, TEST_PASSWORD);

        // Assert
        verify(supabaseAdminClient).createUser(TEST_EMAIL, TEST_PASSWORD);
    }

    @Test
    void authentication_Success() {
        // Arrange
        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Act
        Authentication authentication = authService.authentication(TEST_EMAIL, TEST_PASSWORD);

        // Assert
        assertNotNull(authentication);
        assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
        assertEquals(TEST_EMAIL, authentication.getName());
    }

    @Test
    void authentication_InvalidEmail_ThrowsBadCredentialsException() {
        // Arrange
        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(null);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.authentication(TEST_EMAIL, TEST_PASSWORD));
    }

    @Test
    void authentication_InvalidPassword_ThrowsBadCredentialsException() {
        // Arrange
        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.authentication(TEST_EMAIL, TEST_PASSWORD));
    }

    @Test
    void loginUser_Success() {
        // Arrange
        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(authUser));
        when(jwtUtils.generateTokenFromUsername(userDetails)).thenReturn(JWT_TOKEN);

        // Act
        ResponseEntity<AuthResponse> response = authService.loginUser(TEST_EMAIL, TEST_PASSWORD);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        AuthResponse authResponse = response.getBody();
        assertNotNull(authResponse);
        assertEquals(JWT_TOKEN, authResponse.getJwtToken());
        assertEquals(TEST_EMAIL, authResponse.getEmail());
        assertEquals(USER_ID, authResponse.getUserId());
        assertEquals(1, authResponse.getRoles().size());
        assertEquals("ROLE_USER", authResponse.getRoles().get(0));
    }

    @Test
    void loginUser_InvalidCredentials_ThrowsBadCredentialsException() {
        // Arrange
        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(null);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.loginUser(TEST_EMAIL, TEST_PASSWORD));
    }

    @Test
    void getUserByEmail_Success() {
        // Arrange
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(authUser));

        // Act
        Optional<AuthUser> result = authService.getUserByEmail(TEST_EMAIL);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(USER_ID, result.get().getId());
        assertEquals(TEST_EMAIL, result.get().getEmail());
    }

    @Test
    void getUserByEmail_NotFound() {
        // Arrange
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act
        Optional<AuthUser> result = authService.getUserByEmail(TEST_EMAIL);

        // Assert
        assertFalse(result.isPresent());
    }
}
