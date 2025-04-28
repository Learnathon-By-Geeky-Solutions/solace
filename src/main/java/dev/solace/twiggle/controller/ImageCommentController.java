package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.ImageCommentDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.ImageCommentService;
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
 * REST controller for image comments.
 */
@RestController
@RequestMapping("/api/image-comments")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class ImageCommentController {

    private final ImageCommentService imageCommentService;

    /**
     * Get all image comments with pagination and sorting.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of image comment DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ImageCommentDTO>>> getAllImageComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ImageCommentDTO> comments = imageCommentService.findAll(pageable);
        return ResponseUtil.success("Successfully retrieved image comments", comments);
    }

    /**
     * Get an image comment by ID.
     *
     * @param id the image comment ID
     * @return the image comment DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageCommentDTO>> getImageCommentById(@PathVariable UUID id) {
        return imageCommentService
                .findById(id)
                .map(comment -> ResponseUtil.success("Successfully retrieved image comment", comment))
                .orElseThrow(() -> new CustomException(
                        "Image comment not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Get image comments by image ID with pagination and sorting.
     *
     * @param imageId the image ID
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of image comment DTOs
     */
    @GetMapping("/image/{imageId}")
    public ResponseEntity<ApiResponse<Page<ImageCommentDTO>>> getCommentsByImageId(
            @PathVariable UUID imageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ImageCommentDTO> comments = imageCommentService.findByImageId(imageId, pageable);
        return ResponseUtil.success("Successfully retrieved comments for image", comments);
    }

    /**
     * Get image comments by user ID with pagination and sorting.
     *
     * @param userId the user ID
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of image comment DTOs
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ImageCommentDTO>>> getCommentsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ImageCommentDTO> comments = imageCommentService.findByUserId(userId, pageable);
        return ResponseUtil.success("Successfully retrieved comments by user", comments);
    }

    /**
     * Count comments for an image.
     *
     * @param imageId the image ID
     * @return count of comments
     */
    @GetMapping("/image/{imageId}/count")
    public ResponseEntity<ApiResponse<Long>> countCommentsByImageId(@PathVariable UUID imageId) {
        long count = imageCommentService.countByImageId(imageId);
        return ResponseUtil.success("Successfully counted comments for image", count);
    }

    /**
     * Create a new image comment.
     *
     * @param imageCommentDTO the image comment DTO to create (validated)
     * @return the created image comment DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ImageCommentDTO>> createImageComment(
            @Valid @RequestBody ImageCommentDTO imageCommentDTO) {
        ImageCommentDTO createdComment = imageCommentService.create(imageCommentDTO);
        return ResponseUtil.success("Image comment created successfully", createdComment);
    }

    /**
     * Update an existing image comment.
     *
     * @param id the image comment ID
     * @param imageCommentDTO the updated image comment DTO (validated)
     * @return the updated image comment DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageCommentDTO>> updateImageComment(
            @PathVariable UUID id, @Valid @RequestBody ImageCommentDTO imageCommentDTO) {
        return imageCommentService
                .update(id, imageCommentDTO)
                .map(updatedComment -> ResponseUtil.success("Image comment updated successfully", updatedComment))
                .orElseThrow(() -> new CustomException(
                        "Image comment not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Delete an image comment.
     *
     * @param id the image comment ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImageComment(@PathVariable UUID id) {
        imageCommentService.delete(id);
        return ResponseUtil.success("Image comment deleted successfully", null);
    }

    /**
     * Creates a pageable object from pagination parameters.
     * Throws CustomException if the direction is invalid.
     */
    private Pageable createPageable(int page, int size, String sort, String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            return PageRequest.of(page, size, sortDirection, sort);
        } catch (IllegalArgumentException e) {
            throw new CustomException(
                    "Invalid sort direction. Must be either 'ASC' or 'DESC'",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INVALID_REQUEST);
        }
    }
}
