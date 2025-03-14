package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for just testing out things.
 */
@RestController
@RequestMapping("api/v1")
@RateLimiter(name = "standard-api")
public class TestController {

    /**
     * Test endpoint to check if the application is running.
     *
     * @return ApiResponse with message
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseUtil.success("Test endpoint executed successfully", "Hello, World!");
    }

    /**
     * Test endpoint to check if global error handling is working.
     *
     */
    @RateLimiter(name = "test-error")
    @GetMapping("/test-error")
    public void testError() {
        throw new CustomException("This is a test error", HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST);
    }

    /**
     * Test endpoint to check if global error handling is working for server errors.
     *
     */
    @RateLimiter(name = "test-error")
    @GetMapping("/test-server-error")
    public void testServerError() {
        throw new CustomException("This is a test server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
