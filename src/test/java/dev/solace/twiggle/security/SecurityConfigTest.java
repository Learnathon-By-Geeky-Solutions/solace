package dev.solace.twiggle.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private AuthEntryPointJwt unauthorizedHandler;

    @Mock
    private AuthTokenFilter authTokenFilter;

    @Mock
    private AuthenticationConfiguration authConfig;

    @Mock
    private AuthenticationManager authenticationManager;

    @Captor
    private ArgumentCaptor<HttpSecurity> httpSecurityCaptor;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(dataSource, unauthorizedHandler, authTokenFilter);
    }

    @Test
    void defaultSecurityFilterChain_ShouldConfigureSecurityCorrectly() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.exceptionHandling(any())).thenReturn(http);
        when(http.headers(any())).thenReturn(http);
        when(http.csrf(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(mockFilterChain);

        // Act
        SecurityFilterChain result = securityConfig.defaultSecurityFilterChain(http);

        // Assert
        assertNotNull(result);
        verify(http).authorizeHttpRequests(any());
        verify(http).sessionManagement(any());
        verify(http).exceptionHandling(any());
        verify(http).headers(any());
        verify(http).csrf(any());
        verify(http).addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Test
    void userDetailsService_ShouldReturnJdbcUserDetailsManager() {
        // Act
        UserDetailsService userDetailsService = securityConfig.userDetailsService(dataSource);

        // Assert
        assertTrue(userDetailsService instanceof JdbcUserDetailsManager);
        JdbcUserDetailsManager jdbcUserDetailsManager = (JdbcUserDetailsManager) userDetailsService;
        assertEquals(dataSource, jdbcUserDetailsManager.getDataSource());
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Act
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        // Assert
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void authenticationManager_ShouldReturnAuthenticationManager() throws Exception {
        // Arrange
        when(authConfig.getAuthenticationManager()).thenReturn(authenticationManager);

        // Act
        AuthenticationManager result = securityConfig.authenticationManager(authConfig);

        // Assert
        assertNotNull(result);
        assertEquals(authenticationManager, result);
    }
}
