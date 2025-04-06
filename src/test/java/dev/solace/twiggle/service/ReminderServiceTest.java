package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import dev.solace.twiggle.model.ReminderEmailRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private Resend resendMock;

    @Mock
    private Emails emailsMock;

    private ReminderService reminderService;

    private ReminderEmailRequest validRequest;

    @BeforeEach
    void setUp() throws ResendException {
        // Initialize the service with test values
        reminderService = new ReminderService("test-api-key", "Test <test@example.com>", "https://test-supabase.co");

        // Use reflection to replace the real Resend client with our mock
        ReflectionTestUtils.setField(reminderService, "resend", resendMock);

        // Setup the emails mock
        when(resendMock.emails()).thenReturn(emailsMock);

        // Create a valid test request
        validRequest = new ReminderEmailRequest();
        validRequest.setPlantName("Test Plant");
        validRequest.setReminderType("Water");
        validRequest.setReminderDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        validRequest.setReminderTime("9:00 AM");
        validRequest.setNotes("Test notes");
        validRequest.setUserEmail("test@example.com");
        validRequest.setImageUrl("https://example.com/image.jpg");
        validRequest.setGardenSpaceName("Test Garden");
        validRequest.setGardenSpaceId("test-id-123");
    }

    @Test
    void sendReminderEmail_Success() throws ResendException {
        // Arrange
        CreateEmailResponse mockResponse = new CreateEmailResponse();
        mockResponse.setId("test-email-id-123");

        // Setup the emails mock to return the response
        when(emailsMock.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        // Act
        boolean result = reminderService.sendReminderEmail(validRequest);

        // Assert
        assertTrue(result);
        verify(resendMock, times(1)).emails();
        verify(emailsMock, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmailWithId_Success() throws ResendException {
        // Arrange
        CreateEmailResponse mockResponse = new CreateEmailResponse();
        mockResponse.setId("test-email-id-123");

        // Setup the emails mock to return the response
        when(emailsMock.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        // Act
        Map<String, Object> result = reminderService.sendReminderEmailWithId(validRequest);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("test-email-id-123", result.get("id"));
        verify(resendMock, times(1)).emails();
        verify(emailsMock, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmail_HandlesException() throws ResendException {
        // Arrange
        when(emailsMock.send(any(CreateEmailOptions.class))).thenThrow(new ResendException("Test exception"));

        // Act
        boolean result = reminderService.sendReminderEmail(validRequest);

        // Assert
        assertFalse(result);
        verify(resendMock, times(1)).emails();
        verify(emailsMock, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmailWithId_HandlesException() throws ResendException {
        // Arrange
        when(emailsMock.send(any(CreateEmailOptions.class))).thenThrow(new ResendException("Test exception"));

        // Act
        Map<String, Object> result = reminderService.sendReminderEmailWithId(validRequest);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertFalse(result.containsKey("id"));
        verify(resendMock, times(1)).emails();
        verify(emailsMock, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void verifyEmailContent() throws ResendException {
        // Arrange
        ArgumentCaptor<CreateEmailOptions> emailCaptor = ArgumentCaptor.forClass(CreateEmailOptions.class);
        CreateEmailResponse mockResponse = new CreateEmailResponse();
        mockResponse.setId("test-email-id-123");

        // Setup the emails mock to capture the options and return a response
        when(emailsMock.send(emailCaptor.capture())).thenReturn(mockResponse);

        // Act
        reminderService.sendReminderEmail(validRequest);

        // Assert
        CreateEmailOptions capturedEmail = emailCaptor.getValue();

        // Verify recipient email
        assertEquals(validRequest.getUserEmail(), capturedEmail.getTo().get(0));

        // Verify subject contains plant name and reminder type
        assertTrue(capturedEmail.getSubject().contains(validRequest.getPlantName()));
        assertTrue(capturedEmail.getSubject().contains(validRequest.getReminderType()));

        // Verify HTML content contains key elements
        String htmlContent = capturedEmail.getHtml();
        assertTrue(htmlContent.contains(validRequest.getPlantName()));
        assertTrue(htmlContent.contains(validRequest.getReminderType()));
        assertTrue(htmlContent.contains(validRequest.getImageUrl()));
        assertTrue(htmlContent.contains(validRequest.getGardenSpaceName()));
        assertTrue(htmlContent.contains(validRequest.getNotes()));
    }
}
