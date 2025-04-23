package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.PlantsLibraryDTO;
import dev.solace.twiggle.dto.PlantsLibrarySearchCriteria;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.PlantsLibraryService;
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
 * REST controller for plants library.
 */
@RestController
@RequestMapping("/api/plants-library")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class PlantsLibraryController {

    private final PlantsLibraryService plantsLibraryService;

    /**
     * Get all plants with pagination and sorting.
     *
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plants library DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlantsLibraryDTO>>> getAllPlants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "commonName") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantsLibraryDTO> plants = plantsLibraryService.findAll(pageable);
            return ResponseUtil.success("Successfully retrieved plants", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get all plants without pagination (for backward compatibility).
     *
     * @return list of all plants library DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PlantsLibraryDTO>>> getAllPlantsWithoutPagination() {
        try {
            List<PlantsLibraryDTO> plants = plantsLibraryService.findAll();
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
     * @return the plant library DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantsLibraryDTO>> getPlantById(@PathVariable UUID id) {
        try {
            return plantsLibraryService
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
     * Create a new plant.
     *
     * @param plantsLibraryDTO the plant library DTO to create (validated)
     * @return the created plant library DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PlantsLibraryDTO>> createPlant(
            @Valid @RequestBody PlantsLibraryDTO plantsLibraryDTO) {
        try {
            PlantsLibraryDTO createdPlant = plantsLibraryService.create(plantsLibraryDTO);
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
     * @param id               the plant ID
     * @param plantsLibraryDTO the updated plant library DTO (validated)
     * @return the updated plant library DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantsLibraryDTO>> updatePlant(
            @PathVariable UUID id, @Valid @RequestBody PlantsLibraryDTO plantsLibraryDTO) {
        try {
            return plantsLibraryService
                    .update(id, plantsLibraryDTO)
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
            plantsLibraryService.delete(id);
            return ResponseUtil.success("Plant deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting plant with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to delete plant", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Search plants with various criteria and pagination.
     *
     * @param query     general search term (optional)
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of matching plant library DTOs
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PlantsLibraryDTO>>> searchPlants(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "commonName") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantsLibraryDTO> plants = plantsLibraryService.searchPlants(query, pageable);
            return ResponseUtil.success("Successfully searched plants", plants);
        } catch (Exception e) {
            log.error("Error searching plants: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to search plants", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Advanced search for plants with specific criteria.
     *
     * @param commonName          search by common name (optional)
     * @param otherName           search by other name (optional)
     * @param scientificName      search by scientific name (optional)
     * @param origin              search by origin (optional)
     * @param plantType           search by plant type (optional)
     * @param climate             search by climate (optional)
     * @param lifeCycle           search by life cycle (optional)
     * @param wateringFrequency   search by watering frequency (optional)
     * @param soilType            search by soil type (optional)
     * @param size                search by size (optional)
     * @param sunlightRequirement search by sunlight requirement (optional)
     * @param growthRate          search by growth rate (optional)
     * @param idealPlace          search by ideal place (optional)
     * @param careLevel           search by care level (optional)
     * @param bestPlantingSeason  search by best planting season (optional)
     * @param timeToHarvest       search by time to harvest (optional)
     * @param flower              filter by flower availability (optional)
     * @param fruit               filter by fruit availability (optional)
     * @param medicinal           filter by medicinal property (optional)
     * @param page                page number (0-based)
     * @param pageSize            page size
     * @return page of matching plant library DTOs
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<Page<PlantsLibraryDTO>>> searchPlantsAdvanced(
            @RequestParam(required = false) String commonName,
            @RequestParam(required = false) String otherName,
            @RequestParam(required = false) String scientificName,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String plantType,
            @RequestParam(required = false) String climate,
            @RequestParam(required = false) String lifeCycle,
            @RequestParam(required = false) String wateringFrequency,
            @RequestParam(required = false) String soilType,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String sunlightRequirement,
            @RequestParam(required = false) String growthRate,
            @RequestParam(required = false) String idealPlace,
            @RequestParam(required = false) String careLevel,
            @RequestParam(required = false) String bestPlantingSeason,
            @RequestParam(required = false) Double timeToHarvest,
            @RequestParam(required = false) Boolean flower,
            @RequestParam(required = false) Boolean fruit,
            @RequestParam(required = false) Boolean medicinal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(page, pageSize);
            PlantsLibrarySearchCriteria criteria = PlantsLibrarySearchCriteria.builder()
                    .commonName(commonName)
                    .otherName(otherName)
                    .scientificName(scientificName)
                    .origin(origin)
                    .plantType(plantType)
                    .climate(climate)
                    .lifeCycle(lifeCycle)
                    .wateringFrequency(wateringFrequency)
                    .soilType(soilType)
                    .size(size)
                    .sunlightRequirement(sunlightRequirement)
                    .growthRate(growthRate)
                    .idealPlace(idealPlace)
                    .careLevel(careLevel)
                    .bestPlantingSeason(bestPlantingSeason)
                    .timeToHarvest(timeToHarvest)
                    .flower(flower)
                    .fruit(fruit)
                    .medicinal(medicinal)
                    .build();
            Page<PlantsLibraryDTO> plants = plantsLibraryService.searchPlantsAdvanced(criteria, pageable);
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
     * Get plants by type.
     *
     * @param plantType the plant type
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant library DTOs
     */
    @GetMapping("/type/{plantType}")
    public ResponseEntity<ApiResponse<Page<PlantsLibraryDTO>>> getPlantsByType(
            @PathVariable String plantType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "commonName") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantsLibraryDTO> plants = plantsLibraryService.findByPlantType(plantType, pageable);
            return ResponseUtil.success("Successfully retrieved plants by type", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants by type {}: {}", plantType, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants by type", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get plants by life cycle.
     *
     * @param lifeCycle the life cycle
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant library DTOs
     */
    @GetMapping("/life-cycle/{lifeCycle}")
    public ResponseEntity<ApiResponse<Page<PlantsLibraryDTO>>> getPlantsByLifeCycle(
            @PathVariable String lifeCycle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "commonName") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantsLibraryDTO> plants = plantsLibraryService.findByLifeCycle(lifeCycle, pageable);
            return ResponseUtil.success("Successfully retrieved plants by life cycle", plants);
        } catch (Exception e) {
            log.error("Error retrieving plants by life cycle {}: {}", lifeCycle, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve plants by life cycle",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get medicinal plants.
     *
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant library DTOs
     */
    @GetMapping("/medicinal")
    public ResponseEntity<ApiResponse<Page<PlantsLibraryDTO>>> getMedicinalPlants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "commonName") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<PlantsLibraryDTO> plants = plantsLibraryService.findByMedicinal(true, pageable);
            return ResponseUtil.success("Successfully retrieved medicinal plants", plants);
        } catch (Exception e) {
            log.error("Error retrieving medicinal plants: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve medicinal plants", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }
}
