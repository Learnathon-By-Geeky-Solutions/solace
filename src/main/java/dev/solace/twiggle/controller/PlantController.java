package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.PlantService;
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
 * REST controller for plants.
 */
@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class PlantController {

    private final PlantService plantService;

    /**
     * Get all plants with pagination and sorting.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlantDTO>>> getAllPlants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantDTO> plants = plantService.findAll(pageable);
            return ResponseUtil.success("Successfully retrieved plants", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Search plants with various criteria and pagination.
     *
     * @param query search term for name, description, type, etc. (optional)
     * @param gardenPlanId filter by garden plan ID (optional)
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of matching plant DTOs
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PlantDTO>>> searchPlants(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID gardenPlanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantDTO> plants = plantService.searchPlants(query, gardenPlanId, pageable);
            return ResponseUtil.success("Successfully searched plants", plants);
        } catch (Exception e) {
            log.error("Error searching plants: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to search plants", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Enhanced search for plants with specific fields and closest match capability.
     * This endpoint allows searching by specific fields and returns results ordered by
     * relevance, with closest matches to partial entries ranked higher.
     *
     * @param name search term for plant name (optional)
     * @param type search term for plant type (optional)
     * @param wateringFrequency search term for watering frequency (optional)
     * @param sunlightRequirements search term for sunlight requirements (optional)
     * @param query general search term for any field (optional)
     * @param gardenPlanId filter by garden plan ID (optional)
     * @param page page number (0-based)
     * @param size page size
     * @return page of matching plant DTOs ordered by relevance
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<Page<PlantDTO>>> searchPlantsAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String wateringFrequency,
            @RequestParam(required = false) String sunlightRequirements,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID gardenPlanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // For advanced search, we always sort by relevance score
            Pageable pageable = PageRequest.of(page, size);
            Page<PlantDTO> plants = plantService.searchPlantsWithRelevance(
                    name, type, wateringFrequency, sunlightRequirements, query, gardenPlanId, pageable);
            return ResponseUtil.success("Successfully searched plants with advanced criteria", plants);
        } catch (Exception e) {
            log.error("Error performing advanced search on plants: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to perform advanced search on plants",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get all plants without pagination (for backward compatibility).
     *
     * @return list of all plant DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PlantDTO>>> getAllPlantsWithoutPagination() {
        try {
            List<PlantDTO> plants = plantService.findAll();
            return ResponseUtil.success("Successfully retrieved all plants", plants);
        } catch (Exception e) {
            log.error("Error retrieving all plants: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get a plant by ID.
     *
     * @param id the plant ID
     * @return the plant DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantDTO>> getPlantById(@PathVariable UUID id) {
        try {
            return plantService
                    .findById(id)
                    .map(plant -> ResponseUtil.success("Successfully retrieved plant", plant))
                    .orElseThrow(() ->
                            new CustomException("Plant not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving plant with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plant", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get plants by garden plan ID with pagination and sorting.
     *
     * @param gardenPlanId the garden plan ID
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant DTOs
     */
    @GetMapping("/garden-plan/{gardenPlanId}")
    public ResponseEntity<ApiResponse<Page<PlantDTO>>> getPlantsByGardenPlanId(
            @PathVariable UUID gardenPlanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantDTO> plants = plantService.findByGardenPlanId(gardenPlanId, pageable);
            return ResponseUtil.success("Successfully retrieved plants for garden plan", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants for garden plan {}: {}", gardenPlanId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants for garden plan",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get plants by garden plan ID without pagination (for backward compatibility).
     *
     * @param gardenPlanId the garden plan ID
     * @return list of plant DTOs
     */
    @GetMapping("/garden-plan/{gardenPlanId}/all")
    public ResponseEntity<ApiResponse<List<PlantDTO>>> getPlantsByGardenPlanIdWithoutPagination(
            @PathVariable UUID gardenPlanId) {
        try {
            List<PlantDTO> plants = plantService.findByGardenPlanId(gardenPlanId);
            return ResponseUtil.success("Successfully retrieved plants for garden plan", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants for garden plan {}: {}", gardenPlanId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants for garden plan",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get plants by type with pagination and sorting.
     *
     * @param type the plant type
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant DTOs
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<PlantDTO>>> getPlantsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantDTO> plants = plantService.findByType(type, pageable);
            return ResponseUtil.success("Successfully retrieved plants by type", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants by type {}: {}", type, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants by type", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get plants by type without pagination (for backward compatibility).
     *
     * @param type the plant type
     * @return list of plant DTOs
     */
    @GetMapping("/type/{type}/all")
    public ResponseEntity<ApiResponse<List<PlantDTO>>> getPlantsByTypeWithoutPagination(@PathVariable String type) {
        try {
            List<PlantDTO> plants = plantService.findByType(type);
            return ResponseUtil.success("Successfully retrieved plants by type", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants by type {}: {}", type, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants by type", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Create a new plant.
     *
     * @param plantDTO the plant DTO to create (validated)
     * @return the created plant DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PlantDTO>> createPlant(@Valid @RequestBody PlantDTO plantDTO) {
        try {
            PlantDTO createdPlant = plantService.create(plantDTO);
            return ResponseUtil.success("Plant created successfully", createdPlant);
        } catch (Exception e) {
            log.error("Error creating plant: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to create plant", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Update an existing plant.
     *
     * @param id the plant ID
     * @param plantDTO the updated plant DTO (validated)
     * @return the updated plant DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantDTO>> updatePlant(
            @PathVariable UUID id, @Valid @RequestBody PlantDTO plantDTO) {
        try {
            return plantService
                    .update(id, plantDTO)
                    .map(updatedPlant -> ResponseUtil.success("Plant updated successfully", updatedPlant))
                    .orElseThrow(() ->
                            new CustomException("Plant not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating plant with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to update plant", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Delete a plant.
     *
     * @param id the plant ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlant(@PathVariable UUID id) {
        try {
            plantService.delete(id);
            return ResponseUtil.success("Plant deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting plant with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to delete plant", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }
}
