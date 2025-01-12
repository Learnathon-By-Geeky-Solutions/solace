package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class TestController {

    /**
     * Handles a GET request to the "/test" endpoint and returns a successful response.
     *
     * This method demonstrates a simple test endpoint that returns a greeting message.
     * It uses the {@link ResponseUtil#success(String, Object)} method to create a standardized API response.
     *
     * @return A {@link ResponseEntity} containing an {@link ApiResponse} with a success message and "Hello, World!" data
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseUtil.success("Test endpoint executed successfully", "Hello, World!");
    }

    /**
     * Endpoint to simulate an error scenario for testing purposes.
     *
     * @throws CustomException with a predefined error message and BAD_REQUEST status
     *         when the endpoint is accessed
     */
    @GetMapping("/test-error")
    public void testError() {
        throw new CustomException("This is a test error", HttpStatus.BAD_REQUEST);
    }
}
