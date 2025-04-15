package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.ReminderEmailRequest;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.ReminderService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class ReminderController {

    private final ReminderService reminderService;
    private static final String SUCCESS = "success";

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendReminder(@RequestBody ReminderEmailRequest request) {
        log.info("Received request to send reminder for plant: {}", request.getPlantName());
        try {
            Map<String, Object> result = reminderService.sendReminderEmailWithId(request);
            boolean success = (boolean) result.get(SUCCESS);

            if (success) {
                log.info("Successfully processed reminder request for {}", request.getUserEmail());
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS, true);
                response.put("message", "Reminder email sent successfully");
                if (result.containsKey("id")) {
                    response.put("id", result.get("id"));
                }
                return ResponseUtil.success("Reminder email sent successfully", response);
            } else {
                log.warn("Failed to send reminder email for {}", request.getUserEmail());
                throw new CustomException(
                        "Failed to send reminder email",
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.EMAIL_SENDING_FAILED);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in sendReminder controller: {}", e.getMessage(), e);
            throw new CustomException(
                    "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    @GetMapping("/test/{email}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testEmail(@PathVariable String email) {
        log.info("Sending test email to: {}", email);

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

        try {
            Map<String, Object> result = reminderService.sendReminderEmailWithId(testRequest);
            boolean success = (boolean) result.get(SUCCESS);

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS, true);
                response.put("message", "Test email sent successfully to " + email);
                response.put("id", result.get("id"));
                return ResponseUtil.success("Test email sent successfully", response);
            } else {
                throw new CustomException(
                        "Failed to send test email", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.EMAIL_SENDING_FAILED);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in testEmail controller: {}", e.getMessage(), e);
            throw new CustomException(
                    "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }
}
