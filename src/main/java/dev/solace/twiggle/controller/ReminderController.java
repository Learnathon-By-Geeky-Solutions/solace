package dev.solace.twiggle.controller;

import dev.solace.twiggle.model.ReminderEmailRequest;
import dev.solace.twiggle.service.ReminderService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor // Automatically creates constructor for final fields (ReminderService)
@Slf4j
@CrossOrigin(origins = "*") // For development - restrict to your app's domain in production
public class ReminderController {

    private static final String SUCCESS_KEY = "success";
    private static final String ERROR_KEY = "error";

    private final ReminderService reminderService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendReminder(@RequestBody ReminderEmailRequest request) {
        log.info("Received request to send reminder for plant: {}", request.getPlantName());
        try {
            Map<String, Object> result = reminderService.sendReminderEmailWithId(request);
            boolean success = (boolean) result.get(SUCCESS_KEY);

            if (success) {
                log.info("Successfully processed reminder request for {}", request.getUserEmail());
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS_KEY, true);
                response.put("message", "Reminder email sent successfully");

                // Add the ID if it exists
                if (result.containsKey("id")) {
                    response.put("id", result.get("id"));
                }

                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to send reminder email for {}", request.getUserEmail());
                // Avoid exposing too much detail in the error response
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(SUCCESS_KEY, false, ERROR_KEY, "Failed to send reminder email."));
            }
        } catch (Exception e) {
            log.error("Unexpected error in sendReminder controller: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(SUCCESS_KEY, false, ERROR_KEY, "An unexpected error occurred."));
        }
    }

    /**
     * Test endpoint to quickly send a sample email to the specified recipient
     * For development and testing only - disable in production
     */
    @GetMapping("/test/{email}")
    public ResponseEntity<Map<String, Object>> testEmail(@PathVariable String email) {
        log.info("Sending test email to: {}", email);

        // Create a sample reminder request
        ReminderEmailRequest testRequest = new ReminderEmailRequest();
        testRequest.setPlantName("Test Plant");
        testRequest.setReminderType("Water");
        testRequest.setReminderDate(LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE));
        testRequest.setReminderTime("10:00 AM");
        testRequest.setNotes("This is a test reminder email to verify the functionality works correctly.");
        testRequest.setUserEmail(email);
        testRequest.setImageUrl(
                "https://images.unsplash.com/photo-1463936575829-25148e1db1b8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2090&q=80");
        testRequest.setGardenSpaceName("Test Garden");
        testRequest.setGardenSpaceId("test-garden-id");

        // Send test email
        Map<String, Object> result = reminderService.sendReminderEmailWithId(testRequest);
        boolean success = (boolean) result.get(SUCCESS_KEY);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    SUCCESS_KEY, true, "message", "Test email sent successfully to " + email, "id", result.get("id")));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(SUCCESS_KEY, false, ERROR_KEY, "Failed to send test email"));
        }
    }
}
