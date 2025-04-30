package dev.solace.twiggle.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.service.UserLoadingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserLoadingService userLoadingService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private static final String TEST_USERNAME = "test@example.com";
    private static final String TEST_JWT = "test.jwt.token";

    @BeforeEach
    void setUp() {
        // Clear the security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidJwt_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        UserDetails userDetails = new User(TEST_USERNAME, "password", Collections.emptyList());
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(TEST_JWT);
        when(jwtUtils.validateJwtToken(TEST_JWT)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_JWT)).thenReturn(TEST_USERNAME);
        when(userLoadingService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                TEST_USERNAME,
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_WithInvalidJwt_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(TEST_JWT);
        when(jwtUtils.validateJwtToken(TEST_JWT)).thenReturn(false);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithNoJwt_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(null);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_WithException_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(TEST_JWT);
        when(jwtUtils.validateJwtToken(TEST_JWT)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_JWT)).thenThrow(new RuntimeException("Test exception"));

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void parseJwt_WithValidAuthorizationHeader_ShouldReturnJwt() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(TEST_JWT);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtils).getJwtFromHeader(request);
    }

    @Test
    void doFilterInternal_ShouldAlwaysCallFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test");
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(null);

        // Act
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }
}
