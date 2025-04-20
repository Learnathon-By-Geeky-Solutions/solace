package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.solace.twiggle.config.TestConfig;
import dev.solace.twiggle.dto.ReminderEmailRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class ReminderServiceTest {

    @Autowired
    private ReminderService reminderService;

    @Test
    void sendReminderEmail_ShouldReturnSuccess() {
        // Arrange
        ReminderEmailRequest request = ReminderEmailRequest.builder()
                .userEmail("test@example.com")
                .plantName("Test Plant")
                .reminderType("Watering")
                .reminderDate("2024-05-01")
                .reminderTime("09:00")
                .gardenSpaceName("Test Garden")
                .notes("Sample notes")
                .imageUrl("https://example.com/image.jpg")
                .build();

        // Act
        boolean result = reminderService.sendReminderEmail(request);

        // Assert
        assertTrue(result, "The mock ReminderService should return true");
    }

    @Test
    void sendReminderEmailWithId_ShouldReturnSuccessMap() {
        // Arrange
        ReminderEmailRequest request = ReminderEmailRequest.builder()
                .userEmail("test@example.com")
                .plantName("Test Plant")
                .reminderType("Watering")
                .reminderDate("2024-05-01")
                .reminderTime("09:00")
                .gardenSpaceName("Test Garden")
                .notes("Sample notes")
                .imageUrl("https://example.com/image.jpg")
                .build();

        // Act
        Map<String, Object> result = reminderService.sendReminderEmailWithId(request);

        // Assert
        assertTrue((Boolean) result.get("success"), "The success key should be true");
        assertEquals("mock-email-id", result.get("id"), "The id should match the mock value");
    }
}
