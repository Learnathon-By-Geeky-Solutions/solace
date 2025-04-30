package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.model.AuthUser;
import dev.solace.twiggle.repository.AuthUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserLoadingServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    private UserLoadingService userLoadingService;

    @BeforeEach
    void setUp() {
        userLoadingService = new UserLoadingService(authUserRepository);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";
        String password = "encryptedPassword";
        String role = "ROLE_USER";

        AuthUser authUser = new AuthUser();
        authUser.setEmail(email);
        authUser.setEncryptedPassword(password);
        authUser.setRole(role);

        when(authUserRepository.findByEmail(email)).thenReturn(Optional.of(authUser));

        // Act
        UserDetails userDetails = userLoadingService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role)));

        verify(authUserRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(authUserRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception =
                assertThrows(UsernameNotFoundException.class, () -> userLoadingService.loadUserByUsername(email));

        assertEquals("No user found with email: " + email, exception.getMessage());
        verify(authUserRepository).findByEmail(email);
    }
}
