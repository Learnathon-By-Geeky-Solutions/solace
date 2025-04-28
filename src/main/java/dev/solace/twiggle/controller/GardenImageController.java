package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.GardenImageDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.GardenImageService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
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
 * REST controller for garden images.
 */
@RestController
@RequestMapping("/api/garden-images")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class GardenImageController {

    private final GardenImageService gardenImageService;

    /**
     * Get all garden images with pagination and sorting.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of garden image DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GardenImageDTO>>> getAllGardenImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        return executeWithExceptionHandling("Error retrieving garden images", () -> {
            Pageable pageable = createPageable(page, size, sort, direction);
            Page<GardenImageDTO> images = gardenImageService.findAll(pageable);
            return ResponseUtil.success("Successfully retrieved garden images", images);
        });
    }

    /**
     * Get all garden images without pagination (for backward compatibility).
     *
     * @return list of all garden image DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GardenImageDTO>>> getAllGardenImagesWithoutPagination() {
        return executeWithExceptionHandling("Error retrieving all garden images", () -> {
            List<GardenImageDTO> images = gardenImageService.findAll();
            return ResponseUtil.success("Successfully retrieved all garden images", images);
        });
    }

    /**
     * Get a garden image by ID.
     *
     * @param id the garden image ID
     * @return the garden image DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GardenImageDTO>> getGardenImageById(@PathVariable UUID id) {
        return executeWithExceptionHandling("Error retrieving garden image with id " + id, () -> gardenImageService
                .findById(id)
                .map(image -> ResponseUtil.success("Successfully retrieved garden image", image))
                .orElseThrow(() -> new CustomException(
                        "Garden image not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND)));
    }

    /**
     * Get garden images by garden plan ID with pagination and sorting.
     *
     * @param gardenPlanId the garden plan ID
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of garden image DTOs
     */
    @GetMapping("/garden-plan/{gardenPlanId}")
    public ResponseEntity<ApiResponse<Page<GardenImageDTO>>> getImagesByGardenPlanId(
            @PathVariable UUID gardenPlanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        return executeWithExceptionHandling("Error retrieving images for garden plan " + gardenPlanId, () -> {
            Pageable pageable = createPageable(page, size, sort, direction);
            Page<GardenImageDTO> images = gardenImageService.findByGardenPlanId(gardenPlanId, pageable);
            return ResponseUtil.success("Successfully retrieved images for garden plan", images);
        });
    }

    /**
     * Search garden images by title with pagination and sorting.
     *
     * @param title the title to search for (partial, case-insensitive)
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of garden image DTOs matching the title
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<GardenImageDTO>>> searchImagesByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        return executeWithExceptionHandling("Error searching images by title " + title, () -> {
            Pageable pageable = createPageable(page, size, sort, direction);
            Page<GardenImageDTO> images = gardenImageService.searchByTitle(title, pageable);
            return ResponseUtil.success("Successfully searched images by title", images);
        });
    }

    /**
     * Create a new garden image.
     *
     * @param gardenImageDTO the garden image DTO to create (validated)
     * @return the created garden image DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GardenImageDTO>> createGardenImage(
            @Valid @RequestBody GardenImageDTO gardenImageDTO) {
        return executeWithExceptionHandling("Error creating garden image", () -> {
            GardenImageDTO createdImage = gardenImageService.create(gardenImageDTO);
            return ResponseUtil.success("Garden image created successfully", createdImage);
        });
    }

    /**
     * Update an existing garden image.
     *
     * @param id the garden image ID
     * @param gardenImageDTO the updated garden image DTO (validated)
     * @return the updated garden image DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GardenImageDTO>> updateGardenImage(
            @PathVariable UUID id, @Valid @RequestBody GardenImageDTO gardenImageDTO) {
        return executeWithExceptionHandling("Error updating garden image with id " + id, () -> gardenImageService
                .update(id, gardenImageDTO)
                .map(updatedImage -> ResponseUtil.success("Garden image updated successfully", updatedImage))
                .orElseThrow(() -> new CustomException(
                        "Garden image not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND)));
    }

    /**
     * Delete a garden image.
     *
     * @param id the garden image ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGardenImage(@PathVariable UUID id) {
        return executeWithExceptionHandling("Error deleting garden image with id " + id, () -> {
            gardenImageService.delete(id);
            return ResponseUtil.success("Garden image deleted successfully", null);
        });
    }

    /**
     * Creates a pageable object from pagination parameters.
     */
    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        return PageRequest.of(page, size, sortDirection, sort);
    }

    /**
     * Executes an operation with standardized exception handling.
     */
    private <T> ResponseEntity<ApiResponse<T>> executeWithExceptionHandling(
            String errorLogMessage, Supplier<ResponseEntity<ApiResponse<T>>> operation) {
        try {
            return operation.get();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("{}: {}", errorLogMessage, e.getMessage(), e);
            throw new CustomException(
                    "Failed to process request", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }
}
