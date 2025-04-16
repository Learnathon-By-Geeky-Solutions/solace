package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.plant.PlantRecommendationRequest;
import dev.solace.twiggle.dto.plant.PlantRecommendationResponse;
import dev.solace.twiggle.service.PlantRecommendationService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for plant recommendations powered by AI.
 */
@RestController
@RequestMapping("/api/v1/plant-recommendations")
@RequiredArgsConstructor
@Slf4j
public class PlantRecommendationController {

    private final PlantRecommendationService plantRecommendationService;

    /**
     * Get AI-powered plant recommendations based on user input
     * 
     * @param request the recommendation request
     * @return a response with plant recommendations
     */
    @PostMapping
    @RateLimiter(name = "standard-api")
    public ResponseEntity<ApiResponse<PlantRecommendationResponse>> getRecommendations(
            @RequestBody PlantRecommendationRequest request) {
        
        log.info("Received plant recommendation request for garden type: {}", request.getGardenType());
        
        try {
            if (request.getGardenType() == null || request.getGardenType().isBlank()) {
                log.warn("Rejecting request: Garden type is required");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<PlantRecommendationResponse>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Garden type is required")
                        .build());
            }
            
            if (request.getMessage() == null || request.getMessage().isBlank()) {
                log.warn("Rejecting request: Message is required");
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<PlantRecommendationResponse>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Message is required")
                        .build());
            }
            
            if (request.getExistingPlants() == null) {
                log.debug("Initializing empty existing plants list");
                request.setExistingPlants(new java.util.ArrayList<>());
            }
            
            log.info("Processing plant recommendation request with message: '{}'", request.getMessage());
            PlantRecommendationResponse recommendations = plantRecommendationService.getPlantRecommendations(request);
            
            if (!recommendations.isSuccess()) {
                log.error("Error generating recommendations: {}", recommendations.getError());
                return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<PlantRecommendationResponse>builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(recommendations.getError())
                        .build());
            }
            
            log.info("Successfully generated {} plant recommendations", 
                recommendations.getRecommendations() != null ? recommendations.getRecommendations().size() : 0);
            return ResponseUtil.success("Plant recommendations retrieved successfully", recommendations);
        } catch (Exception e) {
            log.error("Unexpected error processing plant recommendation request", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<PlantRecommendationResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("An unexpected error occurred: " + e.getMessage())
                    .build());
        }
    }
    
    /**
     * Handle any uncaught exceptions from the controller
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception in PlantRecommendationController for path {}: {}", 
            request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.<Void>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error: " + ex.getMessage())
                .build());
    }
} 