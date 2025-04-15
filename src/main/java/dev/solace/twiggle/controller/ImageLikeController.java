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
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<ImageLikeDTO> likes = imageLikeService.findAll(pageable);
            return ResponseUtil.success("Successfully retrieved image likes", likes);
        } catch (Exception e) {
            log.error("Error retrieving image likes: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve image likes", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Get an image like by ID.
     *
     * @param id the image like ID
     * @return the image like DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageLikeDTO>> getImageLikeById(@PathVariable UUID id) {
        try {
            return imageLikeService
                    .findById(id)
                    .map(like -> ResponseUtil.success("Successfully retrieved image like", like))
                    .orElseThrow(() -> new CustomException(
                            "Image like not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving image like with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve image like", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
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
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, sortDirection, sort);
            Page<ImageLikeDTO> likes = imageLikeService.findByImageId(imageId, pageable);
            return ResponseUtil.success("Successfully retrieved likes for image", likes);
        } catch (Exception e) {
            log.error("Error retrieving likes for image {}: {}", imageId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to retrieve likes for image", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Count likes for an image.
     *
     * @param imageId the image ID
     * @return count of likes
     */
    @GetMapping("/image/{imageId}/count")
    public ResponseEntity<ApiResponse<Long>> countLikesByImageId(@PathVariable UUID imageId) {
        try {
            long count = imageLikeService.countByImageId(imageId);
            return ResponseUtil.success("Successfully counted likes for image", count);
        } catch (Exception e) {
            log.error("Error counting likes for image {}: {}", imageId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to count likes for image", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
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
        try {
            boolean hasLiked = imageLikeService.hasUserLikedImage(imageId, userId);
            return ResponseUtil.success("Successfully checked if user liked image", hasLiked);
        } catch (Exception e) {
            log.error("Error checking if user {} liked image {}: {}", userId, imageId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to check if user liked image", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
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
        } catch (Exception e) {
            log.error("Error creating image like: {}", e.getMessage(), e);
            throw new CustomException(
                    "Failed to create image like", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
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
        try {
            boolean liked = imageLikeService.toggleLike(imageId, userId);
            String message = liked ? "Image liked successfully" : "Image unliked successfully";
            return ResponseUtil.success(message, liked);
        } catch (Exception e) {
            log.error("Error toggling like for image {} by user {}: {}", imageId, userId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to toggle image like", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
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
        try {
            boolean unliked = imageLikeService.unlikeImage(imageId, userId);
            return ResponseUtil.success("Image unliked successfully", unliked);
        } catch (Exception e) {
            log.error("Error unliking image {} by user {}: {}", imageId, userId, e.getMessage(), e);
            throw new CustomException(
                    "Failed to unlike image", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Delete an image like by ID.
     *
     * @param id the image like ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteImageLike(@PathVariable UUID id) {
        try {
            imageLikeService.delete(id);
            return ResponseUtil.success("Image like deleted successfully", null);
        } catch (Exception e) {
            log.error("Error deleting image like with id {}: {}", id, e.getMessage(), e);
            throw new CustomException(
                    "Failed to delete image like", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
        }
    }
}
