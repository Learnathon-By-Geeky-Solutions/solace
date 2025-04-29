package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.ImageLikeDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.ImageLikeService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
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
 * REST controller for image likes.
 */
@RestController
@RequestMapping("/api/image-likes")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class ImageLikeController {

    private final ImageLikeService imageLikeService;

    /**
     * Get all image likes with pagination and sorting.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of image like DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ImageLikeDTO>>> getAllImageLikes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        Page<ImageLikeDTO> likes = imageLikeService.findAll(pageable);
        return ResponseUtil.success("Successfully retrieved image likes", likes);
    }

    /**
     * Get an image like by ID.
     *
     * @param id the image like ID
     * @return the image like DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageLikeDTO>> getImageLikeById(@PathVariable UUID id) {
        return imageLikeService
                .findById(id)
                .map(like -> ResponseUtil.success("Successfully retrieved image like", like))
                .orElseThrow(() -> new CustomException(
                        "Image like not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Get image likes by image ID with pagination and sorting.
     *
     * @param imageId the image ID
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of image like DTOs
     */
    @GetMapping("/image/{imageId}")
    public ResponseEntity<ApiResponse<Page<ImageLikeDTO>>> getLikesByImageId(
            @PathVariable UUID imageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
        Page<ImageLikeDTO> likes = imageLikeService.findByImageId(imageId, pageable);
        return ResponseUtil.success("Successfully retrieved likes for image", likes);
    }

    /**
     * Count likes for an image.
     *
     * @param imageId the image ID
     * @return count of likes
     */
    @GetMapping("/image/{imageId}/count")
    public ResponseEntity<ApiResponse<Long>> countLikesByImageId(@PathVariable UUID imageId) {
        long count = imageLikeService.countByImageId(imageId);
        return ResponseUtil.success("Successfully counted likes for image", count);
    }

    /**
     * Check if a user has liked an image.
     *
     * @param imageId the image ID
     * @param userId the user ID
     * @return true if the user has liked the image, false otherwise
     */
    @GetMapping("/image/{imageId}/user/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> hasUserLikedImage(
            @PathVariable UUID imageId, @PathVariable UUID userId) {
        boolean hasLiked = imageLikeService.hasUserLikedImage(imageId, userId);
        return ResponseUtil.success("Successfully checked if user liked image", hasLiked);
    }

    /**
     * Create a new image like.
     *
     * @param imageLikeDTO the image like DTO to create (validated)
     * @return the created image like DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ImageLikeDTO>> createImageLike(@Valid @RequestBody ImageLikeDTO imageLikeDTO) {
        try {
            ImageLikeDTO createdLike = imageLikeService.create(imageLikeDTO);
            return ResponseUtil.success("Image like created successfully", createdLike);
        } catch (IllegalStateException e) {
            log.warn("User has already liked this image: {}", e.getMessage());
            throw new CustomException("User has already liked this image", HttpStatus.CONFLICT, ErrorCode.DUPLICATE);
        }
    }

    /**
     * Toggle like for an image by a user.
     *
     * @param imageId the image ID
     * @param userId the user ID
     * @return true if liked, false if unliked
     */
    @PostMapping("/image/{imageId}/user/{userId}/toggle")
    public ResponseEntity<ApiResponse<Boolean>> toggleImageLike(@PathVariable UUID imageId, @PathVariable UUID userId) {
        boolean liked = imageLikeService.toggleLike(imageId, userId);
        String message = liked ? "Image liked successfully" : "Image unliked successfully";
        return ResponseUtil.success(message, liked);
    }

    /**
     * Delete an image like (unlike an image).
     *
     * @param imageId the image ID
     * @param userId the user ID
     * @return success response
     */
    @DeleteMapping("/image/{imageId}/user/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> unlikeImage(@PathVariable UUID imageId, @PathVariable UUID userId) {
        boolean unliked = imageLikeService.unlikeImage(imageId, userId);
        return ResponseUtil.success("Image unliked successfully", unliked);
    }

    /**
     * Delete an image like by ID.
     *
     * @param id the image like ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImageLike(@PathVariable UUID id) {
        imageLikeService.delete(id);
        return ResponseUtil.success("Image like deleted successfully", null);
    }

    /**
     * Exception handler for all controller methods.
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ApiResponse<Object>> handleExceptions(Exception ex) {
        // Already handled CustomExceptions pass through
        if (ex instanceof CustomException customEx) {
            return new ResponseEntity<>(
                    ApiResponse.builder()
                            .timestamp(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC))
                            .status(customEx.getStatus().value())
                            .message(customEx.getMessage())
                            .data(null)
                            .build(),
                    customEx.getStatus());
        }

        // Handle validation errors
        if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException validationEx) {
            String errorMessage = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .findFirst()
                    .orElse("Validation error");

            return new ResponseEntity<>(
                    ApiResponse.builder()
                            .timestamp(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC))
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(errorMessage)
                            .data(null)
                            .build(),
                    HttpStatus.BAD_REQUEST);
        }

        // Log any unexpected errors
        log.error("Unhandled exception in ImageLikeController: {}", ex.getMessage(), ex);

        // Determine appropriate response
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Failed to process image like operation";

        if (ex instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT;
            message = ex.getMessage();
        }

        return new ResponseEntity<>(
                ApiResponse.builder()
                        .timestamp(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC))
                        .status(status.value())
                        .message(message)
                        .data(null)
                        .build(),
                status);
    }
}
