package dev.solace.twiggle.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.solace.twiggle.model.ReminderEmailRequest;
import dev.solace.twiggle.service.ReminderService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for sending real emails.
 * These tests are disabled by default because they would send actual emails.
 * Enable them manually when you want to test the full email delivery process.
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Enable manually to test actual email sending")
class ReminderEmailIntegrationTest {

    @Autowired
    private ReminderService reminderService;

    @Value("${test.recipient.email:your-test-email@example.com}")
    private String testRecipientEmail;

    private ReminderEmailRequest testRequest;

    @BeforeEach
    void setUp() {
        // Create a valid test request
        testRequest = new ReminderEmailRequest();
        testRequest.setPlantName("Integration Test Plant");
        testRequest.setReminderType("Water");
        testRequest.setReminderDate(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE));
        testRequest.setReminderTime("9:00 AM");
        testRequest.setNotes("This is an integration test email");
        testRequest.setUserEmail(testRecipientEmail);
        testRequest.setImageUrl(
                "https://images.unsplash.com/photo-1463936575829-25148e1db1b8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2090&q=80");
        testRequest.setGardenSpaceName("Integration Test Garden");
        testRequest.setGardenSpaceId("integration-test-id");
    }

    @Test
    void testSendRealEmail() {
        // This test will send a real email when enabled
        // To run it, remove the @Disabled annotation on the class and provide a valid email in
        // application-test.properties

        // Send the email
        Map<String, Object> result = reminderService.sendReminderEmailWithId(testRequest);

        // Check the result
        assertTrue((Boolean) result.get("success"), "Email should be sent successfully");
        System.out.println("Email sent with ID: " + result.get("id"));
    }
}
