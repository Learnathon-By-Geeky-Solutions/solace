package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import dev.solace.twiggle.dto.ReminderEmailRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private Resend resend;

    @Mock
    private Emails emails;

    private ReminderService reminderService;
    private ReminderEmailRequest validRequest;

    @BeforeEach
    void setUp() {
        // Create the service with test configuration
        reminderService = new ReminderService("test-api-key", "test@example.com", "https://test.supabase.co");

        // Mock the Emails service - use lenient() to avoid unnecessary stubbing errors
        lenient().when(resend.emails()).thenReturn(emails);

        // Set the mocked Resend client
        ReflectionTestUtils.setField(reminderService, "resend", resend);

        // Create a valid request for testing
        validRequest = ReminderEmailRequest.builder()
                .userEmail("test@example.com")
                .plantName("Test Plant")
                .reminderType("Watering")
                .reminderDate("2024-05-01")
                .reminderTime("09:00")
                .gardenSpaceName("Test Garden")
                .gardenSpaceId("test-garden-id")
                .notes("Sample notes")
                .imageUrl("https://example.com/image.jpg")
                .build();
    }

    @Test
    void sendReminderEmail_WithValidRequest_ShouldReturnTrue() throws Exception {
        // Arrange
        CreateEmailResponse mockResponse = new CreateEmailResponse();
        mockResponse.setId("test-email-id");
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        // Act
        boolean result = reminderService.sendReminderEmail(validRequest);

        // Assert
        assertTrue(result);
        verify(emails).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmail_WithResendException_ShouldReturnFalse() throws Exception {
        // Arrange
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(new ResendException("Test error"));

        // Act
        boolean result = reminderService.sendReminderEmail(validRequest);

        // Assert
        assertFalse(result);
        verify(emails).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmail_WithNullResendClient_ShouldReturnFalse() {
        // Arrange
        // Save original resend client
        Resend originalResend = (Resend) ReflectionTestUtils.getField(reminderService, "resend");
        // Set resend to null to simulate uninitialized client
        ReflectionTestUtils.setField(reminderService, "resend", null);

        try {
            // Act
            boolean result = reminderService.sendReminderEmail(validRequest);

            // Assert
            assertFalse(result);
            // No verification needed as we're testing the null client case
        } finally {
            // Restore original resend client
            ReflectionTestUtils.setField(reminderService, "resend", originalResend);
        }
    }

    @Test
    void sendReminderEmailWithId_WithValidRequest_ShouldReturnSuccessMap() throws Exception {
        // Arrange
        CreateEmailResponse mockResponse = new CreateEmailResponse();
        mockResponse.setId("test-email-id");
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        // Act
        Map<String, Object> result = reminderService.sendReminderEmailWithId(validRequest);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("test-email-id", result.get("id"));
        verify(emails).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmailWithId_WithResendException_ShouldReturnFailureMap() throws Exception {
        // Arrange
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(new ResendException("Test error"));

        // Act
        Map<String, Object> result = reminderService.sendReminderEmailWithId(validRequest);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertNull(result.get("id"));
        verify(emails).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmailWithId_WithNullResendClient_ShouldReturnFailureMap() {
        // Arrange
        // Save original resend client
        Resend originalResend = (Resend) ReflectionTestUtils.getField(reminderService, "resend");
        // Set resend to null to simulate uninitialized client
        ReflectionTestUtils.setField(reminderService, "resend", null);

        try {
            // Act
            Map<String, Object> result = reminderService.sendReminderEmailWithId(validRequest);

            // Assert
            assertFalse((Boolean) result.get("success"));
            assertNull(result.get("id"));
            // No verification needed as we're testing the null client case
        } finally {
            // Restore original resend client
            ReflectionTestUtils.setField(reminderService, "resend", originalResend);
        }
    }

    @Test
    void sendReminderEmail_WithMinimalRequest_ShouldReturnTrue() throws Exception {
        // Arrange
        CreateEmailResponse mockResponse = new CreateEmailResponse();
        mockResponse.setId("test-email-id");
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        ReminderEmailRequest minimalRequest = ReminderEmailRequest.builder()
                .userEmail("test@example.com")
                .plantName("Test Plant")
                .reminderType("Watering")
                .reminderDate("2024-05-01")
                .reminderTime("09:00")
                .gardenSpaceName("Test Garden")
                .gardenSpaceId("test-garden-id")
                .build();

        // Act
        boolean result = reminderService.sendReminderEmail(minimalRequest);

        // Assert
        assertTrue(result);
        verify(emails).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmail_WithInvalidDate_ShouldStillSendEmail() throws Exception {
        // Arrange
        CreateEmailResponse mockResponse = new CreateEmailResponse();
        mockResponse.setId("test-email-id");
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        ReminderEmailRequest requestWithInvalidDate = ReminderEmailRequest.builder()
                .userEmail("test@example.com")
                .plantName("Test Plant")
                .reminderType("Watering")
                .reminderDate("invalid-date")
                .reminderTime("09:00")
                .gardenSpaceName("Test Garden")
                .gardenSpaceId("test-garden-id")
                .notes("Sample notes")
                .imageUrl("https://example.com/image.jpg")
                .build();

        // Act
        boolean result = reminderService.sendReminderEmail(requestWithInvalidDate);

        // Assert
        assertTrue(result);
        verify(emails).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendReminderEmail_WithGeneralException_ShouldReturnFalse() throws Exception {
        // Arrange
        when(emails.send(any(CreateEmailOptions.class))).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        boolean result = reminderService.sendReminderEmail(validRequest);

        // Assert
        assertFalse(result);
        verify(emails).send(any(CreateEmailOptions.class));
    }
}
