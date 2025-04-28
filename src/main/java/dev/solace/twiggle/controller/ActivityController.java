package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ActivityDTO;
import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.ActivityService;
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
 * REST controller for activities.
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class ActivityController {

    private final ActivityService activityService;

    /**
     * Get all activities with pagination and sorting.
     *
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of activity DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ActivityDTO> activities = activityService.findAll(pageable);
        return ResponseUtil.success("Successfully retrieved activities", activities);
    }

    /**
     * Get all activities without pagination (for backward compatibility).
     *
     * @return list of all activity DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ActivityDTO>>> getAllActivitiesWithoutPagination() {
        List<ActivityDTO> activities = activityService.findAll();
        return ResponseUtil.success("Successfully retrieved all activities", activities);
    }

    /**
     * Get an activity by ID.
     *
     * @param id the activity ID
     * @return the activity DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityDTO>> getActivityById(@PathVariable UUID id) {
        return activityService
                .findById(id)
                .map(activity -> ResponseUtil.success("Successfully retrieved activity", activity))
                .orElseThrow(() ->
                        new CustomException("Activity not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Get activities by user ID with pagination and sorting.
     *
     * @param userId    the user ID
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of activity DTOs
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getActivitiesByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ActivityDTO> activities = activityService.findByUserId(userId, pageable);
        return ResponseUtil.success("Successfully retrieved activities for user", activities);
    }

    /**
     * Get activities by garden plan ID with pagination and sorting.
     *
     * @param gardenPlanId the garden plan ID
     * @param page         page number (0-based)
     * @param size         page size
     * @param sort         sort property
     * @param direction    sort direction (ASC or DESC)
     * @return page of activity DTOs
     */
    @GetMapping("/garden-plan/{gardenPlanId}")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getActivitiesByGardenPlanId(
            @PathVariable UUID gardenPlanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ActivityDTO> activities = activityService.findByGardenPlanId(gardenPlanId, pageable);
        return ResponseUtil.success("Successfully retrieved activities for garden plan", activities);
    }

    /**
     * Get activities by user ID and activity type with pagination and sorting.
     *
     * @param userId       the user ID
     * @param activityType the activity type
     * @param page         page number (0-based)
     * @param size         page size
     * @param sort         sort property
     * @param direction    sort direction (ASC or DESC)
     * @return page of activity DTOs
     */
    @GetMapping("/user/{userId}/type/{activityType}")
    public ResponseEntity<ApiResponse<Page<ActivityDTO>>> getActivitiesByUserIdAndType(
            @PathVariable UUID userId,
            @PathVariable String activityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ActivityDTO> activities = activityService.findByUserIdAndActivityType(userId, activityType, pageable);
        return ResponseUtil.success("Successfully retrieved activities for user and type", activities);
    }

    /**
     * Create a new activity.
     *
     * @param activityDTO the activity DTO to create (validated)
     * @return the created activity DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ActivityDTO>> createActivity(@Valid @RequestBody ActivityDTO activityDTO) {
        ActivityDTO createdActivity = activityService.create(activityDTO);
        return ResponseUtil.success("Activity created successfully", createdActivity);
    }

    /**
     * Update an existing activity.
     *
     * @param id          the activity ID
     * @param activityDTO the updated activity DTO (validated)
     * @return the updated activity DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ActivityDTO>> updateActivity(
            @PathVariable UUID id, @Valid @RequestBody ActivityDTO activityDTO) {
        return activityService
                .update(id, activityDTO)
                .map(updatedActivity -> ResponseUtil.success("Activity updated successfully", updatedActivity))
                .orElseThrow(() ->
                        new CustomException("Activity not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Delete an activity.
     *
     * @param id the activity ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(@PathVariable UUID id) {
        activityService.delete(id);
        return ResponseUtil.success("Activity deleted successfully", null);
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
