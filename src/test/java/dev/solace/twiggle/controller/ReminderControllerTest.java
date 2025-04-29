package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.ReminderEmailRequest;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.ReminderService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReminderController.class)
@Import({RateLimiterConfiguration.class, ReminderControllerTest.ReminderTestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class ReminderControllerTest {

    @TestConfiguration
    static class ReminderTestConfig {
        @Bean
        @Primary
        public ReminderService reminderService() {
            return org.mockito.Mockito.mock(ReminderService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReminderService reminderService;

    @Autowired
    private ObjectMapper objectMapper;

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
        mockServiceResult.put("id", "some-mock-id");

        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenReturn(mockServiceResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/reminders/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Reminder email sent successfully"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.id").value("some-mock-id"));
    }

    @Test
    void sendReminderEmail_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ReminderEmailRequest invalidRequest = ReminderEmailRequest.builder()
                .plantName("Tomato")
                .reminderType("Watering")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/reminders/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
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

        Map<String, Object> failureResult = new HashMap<>();
        failureResult.put("success", false);

        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenReturn(failureResult);

        // Act & Assert
        mockMvc.perform(post("/api/v1/reminders/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testEmail_WithValidEmail_ShouldReturnSuccess() throws Exception {
        // Arrange
        String testEmail = "test@example.com";
        Map<String, Object> mockServiceResult = new HashMap<>();
        mockServiceResult.put("success", true);
        mockServiceResult.put("id", "test-email-id");

        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenReturn(mockServiceResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reminders/test/{email}", testEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Test email sent successfully"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Test email sent successfully to " + testEmail))
                .andExpect(jsonPath("$.data.id").value("test-email-id"));
    }

    @Test
    void testEmail_WhenServiceFails_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        String testEmail = "test@example.com";
        Map<String, Object> failureResult = new HashMap<>();
        failureResult.put("success", false);

        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenReturn(failureResult);

        // Act & Assert
        mockMvc.perform(get("/api/v1/reminders/test/{email}", testEmail)).andExpect(status().isInternalServerError());
    }

    @Test
    void sendReminderEmail_WhenServiceThrowsCustomException_ShouldPropagateException() throws Exception {
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

        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenThrow(new CustomException("Custom error", HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST));

        // Act & Assert
        mockMvc.perform(post("/api/v1/reminders/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendReminderEmail_WhenServiceThrowsUnexpectedException_ShouldReturnInternalServerError() throws Exception {
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

        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/reminders/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testEmail_WhenServiceThrowsCustomException_ShouldPropagateException() throws Exception {
        // Arrange
        String testEmail = "test@example.com";
        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenThrow(new CustomException("Custom error", HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST));

        // Act & Assert
        mockMvc.perform(get("/api/v1/reminders/test/{email}", testEmail)).andExpect(status().isBadRequest());
    }

    @Test
    void testEmail_WhenServiceThrowsUnexpectedException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        String testEmail = "test@example.com";
        when(reminderService.sendReminderEmailWithId(any(ReminderEmailRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/reminders/test/{email}", testEmail)).andExpect(status().isInternalServerError());
    }
}
