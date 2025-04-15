package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.GardenPlanDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.GardenPlanService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for garden plans.
 */
@RestController
@RequestMapping("/api/garden-plans")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class GardenPlanController {

    private final GardenPlanService gardenPlanService;

    /**
     * Get all garden plans.
     *
     * @return list of garden plan DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getAllGardenPlans() {
        try {
            List<GardenPlanDTO> plans = gardenPlanService.findAll();
            return ResponseUtil.success("Successfully retrieved all garden plans", plans);
        } catch (Exception e) {
            log.error("Error retrieving all garden plans: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden plans",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get a garden plan by ID.
     *
     * @param id the garden plan ID
     * @return the garden plan DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GardenPlanDTO>> getGardenPlanById(@PathVariable UUID id) {
        try {
            return gardenPlanService
                    .findById(id)
                    .map(plan -> ResponseUtil.success("Successfully retrieved garden plan", plan))
                    .orElseThrow(() -> new CustomException(
                            "Garden plan not found",
                            HttpStatus.NOT_FOUND,
                            ErrorCode.RESOURCE_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving garden plan with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden plan",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get garden plans by user ID.
     *
     * @param userId the user ID
     * @return list of garden plan DTOs
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getGardenPlansByUserId(@PathVariable UUID userId) {
        try {
            List<GardenPlanDTO> plans = gardenPlanService.findByUserId(userId);
            return ResponseUtil.success("Successfully retrieved user's garden plans", plans);
        } catch (Exception e) {
            log.error("Error retrieving garden plans for user {}: {}", userId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve user's garden plans",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get public garden plans.
     *
     * @return list of public garden plan DTOs
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getPublicGardenPlans() {
        try {
            List<GardenPlanDTO> plans = gardenPlanService.findPublicPlans();
            return ResponseUtil.success("Successfully retrieved public garden plans", plans);
        } catch (Exception e) {
            log.error("Error retrieving public garden plans: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve public garden plans",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Create a new garden plan.
     *
     * @param gardenPlanDTO the garden plan DTO to create (validated)
     * @return the created garden plan DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GardenPlanDTO>> createGardenPlan(@Valid @RequestBody GardenPlanDTO gardenPlanDTO) {
        try {
            GardenPlanDTO createdPlan = gardenPlanService.create(gardenPlanDTO);
            return ResponseUtil.success("Garden plan created successfully", createdPlan);
        } catch (Exception e) {
            log.error("Error creating garden plan: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to create garden plan",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Update an existing garden plan.
     *
     * @param id the garden plan ID
     * @param gardenPlanDTO the updated garden plan DTO (validated)
     * @return the updated garden plan DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GardenPlanDTO>> updateGardenPlan(
            @PathVariable UUID id, @Valid @RequestBody GardenPlanDTO gardenPlanDTO) {
        try {
            return gardenPlanService
                    .update(id, gardenPlanDTO)
                    .map(updatedPlan -> ResponseUtil.success("Garden plan updated successfully", updatedPlan))
                    .orElseThrow(() -> new CustomException(
                            "Garden plan not found",
                            HttpStatus.NOT_FOUND,
                            ErrorCode.RESOURCE_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating garden plan with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to update garden plan",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Delete a garden plan.
     *
     * @param id the garden plan ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGardenPlan(@PathVariable UUID id) {
        try {
            gardenPlanService.delete(id);
            return ResponseUtil.success("Garden plan deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting garden plan with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to delete garden plan",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }
}