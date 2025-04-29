package dev.solace.twiggle.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    private AuthEntryPointJwt authEntryPointJwt;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        authEntryPointJwt = new AuthEntryPointJwt();
        outputStream = new ByteArrayOutputStream();
    }

    private ServletOutputStream createServletOutputStream() {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // Not needed for tests
            }
        };
    }

    @Test
    void commence_ShouldSetResponseStatusAndContentType() throws IOException, ServletException {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/test");
        when(authException.getMessage()).thenReturn("Test error message");
        when(response.getOutputStream()).thenReturn(createServletOutputStream());

        // Act
        authEntryPointJwt.commence(request, response, authException);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
    }

    @Test
    void commence_ShouldWriteCorrectJsonResponse() throws IOException, ServletException {
        // Arrange
        String expectedPath = "/api/test";
        String expectedMessage = "Test error message";
        when(request.getServletPath()).thenReturn(expectedPath);
        when(authException.getMessage()).thenReturn(expectedMessage);
        when(response.getOutputStream()).thenReturn(createServletOutputStream());

        // Act
        authEntryPointJwt.commence(request, response, authException);

        // Assert
        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"status\":401"));
        assertTrue(responseBody.contains("\"error\":\"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\":\"" + expectedMessage + "\""));
        assertTrue(responseBody.contains("\"path\":\"" + expectedPath + "\""));
    }

    @Test
    void commence_ShouldHandleIOException() throws IOException {
        // Arrange
        when(request.getServletPath()).thenReturn("/api/test");
        when(authException.getMessage()).thenReturn("Test error message");
        when(response.getOutputStream()).thenThrow(new IOException("Test IO Exception"));

        // Act & Assert
        assertThrows(IOException.class, () -> authEntryPointJwt.commence(request, response, authException));
    }
}
