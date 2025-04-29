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
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        Page<GardenPlanDTO> plans = gardenPlanService.findAll(pageable);
        return ResponseUtil.success("Successfully retrieved garden plans", plans);
    }

    /**
     * Get all garden plans without pagination (for backward compatibility).
     *
     * @return list of all garden plan DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getAllGardenPlansWithoutPagination() {
        List<GardenPlanDTO> plans = gardenPlanService.findAll();
        return ResponseUtil.success("Successfully retrieved all garden plans", plans);
    }

    /**
     * Get a garden plan by ID.
     *
     * @param id the garden plan ID
     * @return the garden plan DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GardenPlanDTO>> getGardenPlanById(@PathVariable UUID id) {
        return gardenPlanService
                .findById(id)
                .map(plan -> ResponseUtil.success("Successfully retrieved garden plan", plan))
                .orElseThrow(() -> new CustomException(
                        "Garden plan not found with id: " + id, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
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
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        Page<GardenPlanDTO> plans = gardenPlanService.findByUserId(userId, pageable);
        return ResponseUtil.success("Successfully retrieved user's garden plans", plans);
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
        List<GardenPlanDTO> plans = gardenPlanService.findByUserId(userId);
        return ResponseUtil.success("Successfully retrieved user's garden plans", plans);
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
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        Page<GardenPlanDTO> plans = gardenPlanService.findPublicPlans(pageable);
        return ResponseUtil.success("Successfully retrieved public garden plans", plans);
    }

    /**
     * Get public garden plans without pagination (for backward compatibility).
     *
     * @return list of public garden plan DTOs
     */
    @GetMapping("/public/all")
    public ResponseEntity<ApiResponse<List<GardenPlanDTO>>> getPublicGardenPlansWithoutPagination() {
        List<GardenPlanDTO> plans = gardenPlanService.findPublicPlans();
        return ResponseUtil.success("Successfully retrieved public garden plans", plans);
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
        GardenPlanDTO createdPlan = gardenPlanService.create(gardenPlanDTO);
        return ResponseUtil.success("Garden plan created successfully", createdPlan);
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
        return gardenPlanService
                .update(id, gardenPlanDTO)
                .map(updatedPlan -> ResponseUtil.success("Garden plan updated successfully", updatedPlan))
                .orElseThrow(() -> new CustomException(
                        "Garden plan not found with id: " + id + " for update",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Delete a garden plan.
     *
     * @param id the garden plan ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGardenPlan(@PathVariable UUID id) {
        gardenPlanService.delete(id);
        return ResponseUtil.success("Garden plan deleted successfully", null);
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
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        Page<GardenPlanDTO> plans = gardenPlanService.searchGardenPlans(query, userId, isPublic, pageable);
        return ResponseUtil.success("Successfully searched garden plans", plans);
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
        Pageable pageable = PageRequest.of(page, size);
        Page<GardenPlanDTO> plans = gardenPlanService.searchGardenPlansWithRelevance(
                name, type, location, query, userId, isPublic, pageable);
        return ResponseUtil.success("Successfully searched garden plans with advanced criteria", plans);
    }
}
