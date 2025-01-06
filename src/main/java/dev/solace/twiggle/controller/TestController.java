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

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseUtil.success("Test endpoint executed successfully", "Hello, World!");
    }

    @GetMapping("/test-error")
    public void testError() {
        throw new CustomException("This is a test error", HttpStatus.BAD_REQUEST);
    }
}
