package dev.solace.twiggle.config;

import dev.solace.twiggle.dto.ReminderEmailRequest;
import dev.solace.twiggle.service.ReminderService;
import java.util.HashMap;
import java.util.Map;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides mock beans to replace services
 * that depend on external environment variables.
 */
@TestConfiguration
public class TestConfig {

    private static final String SUCCESS_KEY = "success";

    /**
     * Provides a mock ReminderService that doesn't depend on the RESEND_API_KEY
     * environment variable.
     */
    @Bean
    @Primary
    public ReminderService reminderService() {
        ReminderService mockReminderService = Mockito.mock(ReminderService.class);

        // Configure mock to return success by default
        Map<String, Object> successResult = new HashMap<>();
        successResult.put(SUCCESS_KEY, true);
        successResult.put("id", "mock-email-id");

        Mockito.when(mockReminderService.sendReminderEmailWithId(Mockito.any(ReminderEmailRequest.class)))
                .thenReturn(successResult);

        Mockito.when(mockReminderService.sendReminderEmail(Mockito.any(ReminderEmailRequest.class)))
                .thenReturn(true);

        return mockReminderService;
    }
}
