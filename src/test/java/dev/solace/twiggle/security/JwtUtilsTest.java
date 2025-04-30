package dev.solace.twiggle.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        // Generating a token that specifically triggers UnsupportedJwtException is complex
        // without more intricate setup (e.g., using a different algorithm or key type).
        // This assertion serves as a placeholder for the expected outcome.
        String potentiallyUnsupportedToken = "some.unsupported.token"; // Placeholder

        // Act & Assert
        assertFalse(
                jwtUtils.validateJwtToken(potentiallyUnsupportedToken),
                "Validation should fail for an unsupported token format.");
        // Consider using advanced mocking (e.g., PowerMock or Mockito's static mocking features if available)
        // to specifically mock the Jwts.parser() chain and throw UnsupportedJwtException
        // if strict verification of the catch block is required.
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
