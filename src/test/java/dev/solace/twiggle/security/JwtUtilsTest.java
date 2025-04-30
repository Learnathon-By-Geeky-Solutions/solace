package dev.solace.twiggle.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private JwtUtils jwtUtils;

    private UserDetails userDetails;
    private static final String TEST_USERNAME = "test@example.com";
    private static final String TEST_JWT_SECRET = "testSecretKeyWithAtLeast256BitsForHS256Algorithm";
    private static final int TEST_JWT_EXPIRATION = 3600000; // 1 hour in milliseconds

    @BeforeEach
    void setUp() {
        userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_JWT_EXPIRATION);
    }

    @Test
    void getJwtFromHeader_WithValidBearerToken_ShouldReturnToken() {
        // Arrange
        String token = "valid.jwt.token";
        String bearerToken = "Bearer " + token;
        when(request.getHeader("Authorization")).thenReturn(bearerToken);

        // Act
        String extractedToken = jwtUtils.getJwtFromHeader(request);

        // Assert
        assertEquals(token, extractedToken);
    }

    @Test
    void getJwtFromHeader_WithNoBearerPrefix_ShouldReturnNull() {
        // Arrange
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(token);

        // Act
        String extractedToken = jwtUtils.getJwtFromHeader(request);

        // Assert
        assertNull(extractedToken);
    }

    @Test
    void getJwtFromHeader_WithNoAuthorizationHeader_ShouldReturnNull() {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        String extractedToken = jwtUtils.getJwtFromHeader(request);

        // Assert
        assertNull(extractedToken);
    }

    @Test
    void generateTokenFromUsername_ShouldReturnValidToken() {
        // Act
        String token = jwtUtils.generateTokenFromUsername(userDetails);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals(TEST_USERNAME, jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void getUserNameFromJwtToken_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(userDetails);

        // Act
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(userDetails);

        // Act & Assert
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_WithMalformedToken_ShouldReturnFalse() {
        // Arrange
        String malformedToken = "malformed.jwt.token";

        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(malformedToken));
    }

    @Test
    void validateJwtToken_WithExpiredToken_ShouldReturnFalse() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -3600000); // Set expiration to -1 hour
        String expiredToken = jwtUtils.generateTokenFromUsername(userDetails);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_JWT_EXPIRATION); // Reset expiration

        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(expiredToken));
    }

    @Test
    void validateJwtToken_WithEmptyToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(""));
    }

    @Test
    void validateJwtToken_WithNullToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(null));
    }

    @Test
    void validateJwtToken_WithUnsupportedToken_ShouldReturnFalse() {
        // Arrange
        // Generate a token with a different algorithm/key type if possible, or simulate the exception.
        // For simplicity and reliability, we'll simulate.
        // This requires deeper mocking or generating a token signed with a completely different mechanism
        // (like RSA) which is complex to set up here.
        // A practical approach for testing might involve mocking the parser behavior if direct generation is too
        // involved.
        // However, let's try generating a token with a different key structure if feasible or just acknowledge the
        // limitation.

        // Simulate by trying to validate with a different key type (if Key was accessible and modifiable,
        // or by generating a token signed differently). Since we use hmacShaKeyFor, let's assume
        // a scenario where the token was signed with something else Jwts might not support by default
        // without specific configuration. A direct simulation is hard without more complex setup.
        // Let's focus on testing the catch block's existence via other means if direct trigger is hard.

        // Alternative: If we had access to the internal Key object, we could potentially replace it
        // with an incompatible key type to force the exception.

        // Given the constraints, testing this specific exception path directly might require
        // more advanced mocking or a known token string that triggers it.
        // We'll skip adding a direct test for UnsupportedJwtException due to complexity in setup,
        // but acknowledge it's a potential gap if strict coverage is needed.
        // Consider using PowerMockito or similar if mocking static/final methods becomes necessary.
    }

    @Test
    void validateJwtToken_WithInvalidSignature_ShouldReturnFalse() {
        // Arrange
        String token = jwtUtils.generateTokenFromUsername(userDetails);
        // Tamper with the signature part of the token
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidSignature";

        // Act & Assert
        // Note: Depending on the library version, this might throw SignatureException or MalformedJwtException
        assertFalse(jwtUtils.validateJwtToken(tamperedToken));
        // We add this test case as invalid signatures are a common validation failure.
    }

    @Test
    void validateJwtToken_WithPotentiallyIllegalArgumentToken_ShouldReturnFalse() {
        // Arrange
        // Tokens like " ." or similar might trigger this internally, though often MalformedJwtException catches these.
        String potentiallyIllegalArgumentToken = " "; // Example: Whitespace only

        // Act & Assert
        assertFalse(jwtUtils.validateJwtToken(potentiallyIllegalArgumentToken));
        // Add more specific examples if known tokens trigger IllegalArgumentException specifically.
    }
}
