package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.ActivityDTO;
import dev.solace.twiggle.mapper.ActivityMapper;
import dev.solace.twiggle.model.Activity;
import dev.solace.twiggle.repository.ActivityRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing activities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    /**
     * Find all activities with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of activity DTOs
     */
    public Page<ActivityDTO> findAll(Pageable pageable) {
        return activityRepository.findAll(pageable).map(activityMapper::toDto);
    }

    /**
     * Find all activities without pagination.
     *
     * @return list of all activity DTOs
     */
    public List<ActivityDTO> findAll() {
        return activityRepository.findAll().stream().map(activityMapper::toDto).toList();
    }

    /**
     * Find activity by ID.
     *
     * @param id the activity ID
     * @return optional containing the activity DTO if found
     */
    public Optional<ActivityDTO> findById(UUID id) {
        return activityRepository.findById(id).map(activityMapper::toDto);
    }

    /**
     * Find activities by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination and sorting parameters
     * @return page of activity DTOs for the user
     */
    public Page<ActivityDTO> findByUserId(UUID userId, Pageable pageable) {
        return activityRepository.findByUserId(userId, pageable).map(activityMapper::toDto);
    }

    /**
     * Find activities by user ID without pagination.
     *
     * @param userId the user ID
     * @return list of activity DTOs for the user
     */
    public List<ActivityDTO> findByUserId(UUID userId) {
        return activityRepository.findByUserId(userId).stream()
                .map(activityMapper::toDto)
                .toList();
    }

    /**
     * Find activities by garden plan ID with pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @param pageable pagination and sorting parameters
     * @return page of activity DTOs for the garden plan
     */
    public Page<ActivityDTO> findByGardenPlanId(UUID gardenPlanId, Pageable pageable) {
        return activityRepository.findByGardenPlanId(gardenPlanId, pageable).map(activityMapper::toDto);
    }

    /**
     * Find activities by garden plan ID without pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @return list of activity DTOs for the garden plan
     */
    public List<ActivityDTO> findByGardenPlanId(UUID gardenPlanId) {
        return activityRepository.findByGardenPlanId(gardenPlanId).stream()
                .map(activityMapper::toDto)
                .toList();
    }

    /**
     * Find activities by user ID and activity type with pagination.
     *
     * @param userId the user ID
     * @param activityType the activity type
     * @param pageable pagination and sorting parameters
     * @return page of activity DTOs for the user and type
     */
    public Page<ActivityDTO> findByUserIdAndActivityType(UUID userId, String activityType, Pageable pageable) {
        return activityRepository
                .findByUserIdAndActivityType(userId, activityType, pageable)
                .map(activityMapper::toDto);
    }

    /**
     * Find activities by garden plan ID and activity type with pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @param activityType the activity type
     * @param pageable pagination and sorting parameters
     * @return page of activity DTOs for the garden plan and type
     */
    public Page<ActivityDTO> findByGardenPlanIdAndActivityType(
            UUID gardenPlanId, String activityType, Pageable pageable) {
        return activityRepository
                .findByGardenPlanIdAndActivityType(gardenPlanId, activityType, pageable)
                .map(activityMapper::toDto);
    }

    /**
     * Create a new activity.
     *
     * @param activityDTO the activity DTO to create
     * @return the created activity DTO
     */
    @Transactional
    public ActivityDTO create(ActivityDTO activityDTO) {
        activityDTO.setCreatedAt(OffsetDateTime.now());

        Activity activity = activityMapper.toEntity(activityDTO);
        Activity savedActivity = activityRepository.save(activity);

        return activityMapper.toDto(savedActivity);
    }

    /**
     * Update an existing activity.
     *
     * @param id the activity ID
     * @param activityDTO the updated activity details
     * @return the updated activity DTO if found
     */
    @Transactional
    public Optional<ActivityDTO> update(UUID id, ActivityDTO activityDTO) {
        return activityRepository.findById(id).map(existingActivity -> {
            // Update fields from the DTO
            existingActivity.setUserId(activityDTO.getUserId());
            existingActivity.setGardenPlanId(activityDTO.getGardenPlanId());
            existingActivity.setActivityType(activityDTO.getActivityType());
            existingActivity.setDescription(activityDTO.getDescription());

            // Save and convert back to DTO
            return activityMapper.toDto(activityRepository.save(existingActivity));
        });
    }

    /**
     * Delete an activity by ID.
     *
     * @param id the activity ID
     */
    @Transactional
    public void delete(UUID id) {
        activityRepository.deleteById(id);
    }
}
