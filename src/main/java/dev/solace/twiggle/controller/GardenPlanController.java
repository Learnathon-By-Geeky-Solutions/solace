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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * Get all garden plans with pagination and sorting.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of garden plan DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GardenPlanDTO>>> getAllGardenPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<GardenPlanDTO> plans = gardenPlanService.findAll(pageable);
            return ResponseUtil.success("Successfully retrieved garden plans", plans);
        } catch (Exception e) {
            log.error("Error retrieving garden plans: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden plans", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get all garden plans without pagination (for backward compatibility).
     *
     * @return list of all garden plan DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getAllGardenPlansWithoutPagination() {
        try {
            List<GardenPlanDTO> plans = gardenPlanService.findAll();
            return ResponseUtil.success("Successfully retrieved all garden plans", plans);
        } catch (Exception e) {
            log.error("Error retrieving all garden plans: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden plans", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
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
                            "Garden plan not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving garden plan with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve garden plan", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get garden plans by user ID with pagination and sorting.
     *
     * @param userId the user ID
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of garden plan DTOs
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<GardenPlanDTO>>> getGardenPlansByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<GardenPlanDTO> plans = gardenPlanService.findByUserId(userId, pageable);
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
     * Get garden plans by user ID without pagination (for backward compatibility).
     *
     * @param userId the user ID
     * @return list of garden plan DTOs
     */
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getGardenPlansByUserIdWithoutPagination(
            @PathVariable UUID userId) {
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
     * Get public garden plans with pagination and sorting.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of public garden plan DTOs
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<GardenPlanDTO>>> getPublicGardenPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<GardenPlanDTO> plans = gardenPlanService.findPublicPlans(pageable);
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
     * Get public garden plans without pagination (for backward compatibility).
     *
     * @return list of public garden plan DTOs
     */
    @GetMapping("/public/all")
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getPublicGardenPlansWithoutPagination() {
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
    public ResponseEntity<ApiResponse<GardenPlanDTO>> createGardenPlan(
            @Valid @RequestBody GardenPlanDTO gardenPlanDTO) {
        try {
            GardenPlanDTO createdPlan = gardenPlanService.create(gardenPlanDTO);
            return ResponseUtil.success("Garden plan created successfully", createdPlan);
        } catch (Exception e) {
            log.error("Error creating garden plan: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to create garden plan", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
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
                            "Garden plan not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating garden plan with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to update garden plan", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
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
                    "Failed to delete garden plan", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Search garden plans with various criteria and pagination.
     *
     * @param query search term for name, description, type, or location (optional)
     * @param userId filter by user ID (optional)
     * @param isPublic filter by public status (optional)
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of matching garden plan DTOs
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<GardenPlanDTO>>> searchGardenPlans(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<GardenPlanDTO> plans = gardenPlanService.searchGardenPlans(query, userId, isPublic, pageable);
            return ResponseUtil.success("Successfully searched garden plans", plans);
        } catch (Exception e) {
            log.error("Error searching garden plans: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to search garden plans", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Enhanced search for garden plans with specific fields and closest match capability.
     * This endpoint allows searching by specific fields and returns results ordered by
     * relevance, with the closest matches to partial entries ranked higher.
     *
     * @param name search term for garden plan name (optional)
     * @param type search term for garden plan type (optional)
     * @param location search term for garden plan location (optional)
     * @param query general search term for any field (optional)
     * @param userId filter by user ID (optional)
     * @param isPublic filter by public status (optional)
     * @param page page number (0-based)
     * @param size page size
     * @return page of matching garden plan DTOs ordered by relevance
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<Page<GardenPlanDTO>>> searchGardenPlansAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // For advanced search, we always sort by relevance score
            Pageable pageable = PageRequest.of(page, size);
            Page<GardenPlanDTO> plans = gardenPlanService.searchGardenPlansWithRelevance(
                    name, type, location, query, userId, isPublic, pageable);
            return ResponseUtil.success("Successfully searched garden plans with advanced criteria", plans);
        } catch (Exception e) {
            log.error("Error performing advanced search on garden plans: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to perform advanced search on garden plans",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }
}
