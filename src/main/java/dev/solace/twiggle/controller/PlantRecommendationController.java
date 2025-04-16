package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import dev.solace.twiggle.service.PlantRecommendationService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for plant recommendations powered by AI.
 */
@RestController
@RequestMapping("/api/v1/plant-recommendations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PlantRecommendationController {

    private final PlantRecommendationService plantRecommendationService;

    /**
     * Get AI-powered plant recommendations based on user input.
     */
    @PostMapping
    @RateLimiter(name = "standard-api")
    public ResponseEntity<ApiResponse<PlantRecommendationResponse>> getRecommendations(
            @Valid @RequestBody PlantRecommendationRequest request) {

        log.info("Received plant recommendation request for garden type: {}", request.getGardenType());

        try {
            // Apply default values to missing/blank fields
            request.applyDefaultsIfNeeded();

            log.info("Processing plant recommendation request with message: '{}'", request.getMessage());

            PlantRecommendationResponse recommendations = plantRecommendationService.getPlantRecommendations(request);

            if (!recommendations.isSuccess()) {
                log.error("Error generating recommendations: {}", recommendations.getError());
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, recommendations.getError());
            }

            int count = recommendations.getRecommendations() != null
                    ? recommendations.getRecommendations().size()
                    : 0;

            log.info("Successfully generated {} plant recommendations", count);

            return ResponseUtil.success("Plant recommendations retrieved successfully", recommendations);
        } catch (Exception e) {
            log.error("Unexpected error processing plant recommendation request", e);
            return buildErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Fallback handler for unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex, HttpServletRequest request) {
        log.error(
                "Unhandled exception in PlantRecommendationController for path {}: {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + ex.getMessage());
    }

    /**
     * Utility method to build error responses.
     */
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.<T>builder()
                        .status(status.value())
                        .message(message)
                        .build());
    }
}
