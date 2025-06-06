package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.ProfileDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.ProfileService;
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
 * REST controller for user profiles.
 */
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get all profiles with pagination and sorting.
     *
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of profile DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProfileDTO>>> getAllProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ProfileDTO> profiles = profileService.findAll(pageable);
        return ResponseUtil.success("Successfully retrieved profiles", profiles);
    }

    /**
     * Search profiles with various criteria and pagination.
     *
     * @param query search term for full name or other fields (optional)
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of matching profile DTOs
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProfileDTO>>> searchProfiles(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ProfileDTO> profiles = profileService.searchProfiles(query, pageable);
        return ResponseUtil.success("Successfully searched profiles", profiles);
    }

    /**
     * Enhanced search for profiles with the closest match capability.
     * This endpoint allows searching specifically by full name and returns results ordered by
     * relevance, with the closest matches to partial entries ranked higher.
     *
     * @param fullName search term for full name (optional)
     * @param query general search term for any field (optional)
     * @param page page number (0-based)
     * @param size page size
     * @return page of matching profile DTOs ordered by relevance
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<Page<ProfileDTO>>> searchProfilesAdvanced(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // For advanced search, we always sort by relevance score
        Pageable pageable = PageRequest.of(page, size);
        Page<ProfileDTO> profiles = profileService.searchProfilesWithRelevance(fullName, query, pageable);
        return ResponseUtil.success("Successfully searched profiles with advanced criteria", profiles);
    }

    /**
     * Get all profiles without pagination (for backward compatibility).
     *
     * @return list of all profile DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ProfileDTO>>> getAllProfilesWithoutPagination() {
        List<ProfileDTO> profiles = profileService.findAll();
        return ResponseUtil.success("Successfully retrieved all profiles", profiles);
    }

    /**
     * Get a profile by ID.
     *
     * @param id the profile ID
     * @return the profile DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfileDTO>> getProfileById(@PathVariable UUID id) {
        return profileService
                .findById(id)
                .map(profile -> ResponseUtil.success("Successfully retrieved profile", profile))
                .orElseThrow(() ->
                        new CustomException("Profile not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Get profiles by name with pagination and sorting.
     *
     * @param fullName the name to search for
     * @param page page number (0-based)
     * @param size page size
     * @param sort sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of profile DTOs
     */
    @GetMapping("/name/{fullName}")
    public ResponseEntity<ApiResponse<Page<ProfileDTO>>> getProfilesByName(
            @PathVariable String fullName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<ProfileDTO> profiles = profileService.findByFullName(fullName, pageable);
        return ResponseUtil.success("Successfully retrieved profiles by name", profiles);
    }

    /**
     * Get profiles by name without pagination (for backward compatibility).
     *
     * @param fullName the name to search for
     * @return list of profile DTOs
     */
    @GetMapping("/name/{fullName}/all")
    public ResponseEntity<ApiResponse<List<ProfileDTO>>> getProfilesByNameWithoutPagination(
            @PathVariable String fullName) {
        List<ProfileDTO> profiles = profileService.findByFullName(fullName);
        return ResponseUtil.success("Successfully retrieved profiles by name", profiles);
    }

    /**
     * Create a new profile.
     *
     * @param profileDTO the profile DTO to create (validated)
     * @return the created profile DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProfileDTO>> createProfile(@Valid @RequestBody ProfileDTO profileDTO) {
        ProfileDTO createdProfile = profileService.create(profileDTO);
        return ResponseUtil.created("Profile created successfully", createdProfile);
    }

    /**
     * Update an existing profile.
     *
     * @param id the profile ID
     * @param profileDTO the updated profile DTO (validated)
     * @return the updated profile DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfileDTO>> updateProfile(
            @PathVariable UUID id, @Valid @RequestBody ProfileDTO profileDTO) {
        return profileService
                .update(id, profileDTO)
                .map(updatedProfile -> ResponseUtil.success("Profile updated successfully", updatedProfile))
                .orElseThrow(() ->
                        new CustomException("Profile not found", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Delete a profile.
     *
     * @param id the profile ID
     * @return success response or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable UUID id) {
        // Check if profile exists first
        if (!profileService.findById(id).isPresent()) {
            throw new CustomException(
                    "Profile not found for deletion", HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
        }
        profileService.delete(id);
        return ResponseUtil.success("Profile deleted successfully", null);
    }

    // Helper method to create Pageable objects
    private Pageable createPageable(int page, int size, String sort, String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            return PageRequest.of(page, size, sortDirection, sort);
        } catch (IllegalArgumentException e) {
            log.warn(
                    "Invalid sort direction provided: '{}'. Defaulting might be required or throw bad request.",
                    direction);
            throw new CustomException(
                    "Invalid sorting parameter: direction must be ASC or DESC",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.INVALID_REQUEST);
        }
    }
}
