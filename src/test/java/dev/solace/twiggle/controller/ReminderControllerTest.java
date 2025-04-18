package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.ReminderEmailRequest;
import dev.solace.twiggle.service.ReminderService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReminderController.class)
@Import({RateLimiterConfiguration.class})
class ReminderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReminderService reminderService;

    @Autowired
    private ObjectMapper objectMapper; // Inject ObjectMapper

    @Test
    void sendReminderEmail_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Arrange
        ReminderEmailRequest request = ReminderEmailRequest.builder()
                .userEmail("user@example.com")
                .plantName("Tomato")
                .reminderType("Watering")
                .reminderDate("2024-07-20")
                .reminderTime("09:00")
                .gardenSpaceName("Backyard Patch")
                .gardenSpaceId(UUID.randomUUID().toString())
                .build();

        // Mock the correct service method to return a success Map
        Map<String, Object> mockServiceResult = new HashMap<>();
        mockServiceResult.put("success", true);
        mockServiceResult.put("id", "some-mock-id"); // Optional: include ID if controller uses it

        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenReturn(mockServiceResult);

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/reminders/send") // Assuming endpoint remains /send
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Reminder email sent successfully"));
        // Adjust assertion if the response includes the email ID
    }

    @Test
    void sendReminderEmail_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        // Example: Missing required fields like userEmail
        ReminderEmailRequest invalidRequest = ReminderEmailRequest.builder()
                .plantName("Tomato")
                .reminderType("Watering")
                .build();

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/reminders/send") // Assuming endpoint remains /send
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expecting validation failure
    }

    @Test
    void sendReminderEmail_WhenServiceFails_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        ReminderEmailRequest request = ReminderEmailRequest.builder()
                .userEmail("user@example.com")
                .plantName("Tomato")
                .reminderType("Watering")
                .reminderDate("2024-07-20")
                .reminderTime("09:00")
                .gardenSpaceName("Backyard Patch")
                .gardenSpaceId(UUID.randomUUID().toString())
                .build();

        // Assuming the controller uses the boolean returning method
        when(reminderService.sendReminderEmail(any(ReminderEmailRequest.class))).thenReturn(false);
        // Or if it throws an exception:
        // when(reminderService.sendReminderEmail(any(ReminderEmailRequest.class)))
        //      .thenThrow(new RuntimeException("Email service unavailable"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/reminders/send") // Assuming endpoint remains /send
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Or map based on boolean return
        // Adjust expected status based on how the controller handles service failure (boolean vs exception)
        // If the controller returns 500 based on `false` return:
        // .andExpect(jsonPath("$.status").value(500))
        // .andExpect(jsonPath("$.message").value("Failed to send reminder email"));

    }
}
