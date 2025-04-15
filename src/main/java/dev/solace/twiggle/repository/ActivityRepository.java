package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.Activity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Activity entities.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    /**
     * Find all activities for a specific user with pagination.
     *
     * @param userId The ID of the user
     * @param pageable Pagination and sorting information
     * @return Page of activities for the user
     */
    Page<Activity> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all activities for a specific user.
     *
     * @param userId The ID of the user
     * @return List of activities for the user
     */
    List<Activity> findByUserId(UUID userId);

    /**
     * Find all activities for a specific garden plan with pagination.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param pageable Pagination and sorting information
     * @return Page of activities for the garden plan
     */
    Page<Activity> findByGardenPlanId(UUID gardenPlanId, Pageable pageable);

    /**
     * Find all activities for a specific garden plan.
     *
     * @param gardenPlanId The ID of the garden plan
     * @return List of activities for the garden plan
     */
    List<Activity> findByGardenPlanId(UUID gardenPlanId);

    /**
     * Find all activities of a specific type for a user with pagination.
     *
     * @param userId The ID of the user
     * @param activityType The type of activity
     * @param pageable Pagination and sorting information
     * @return Page of activities of the specified type for the user
     */
    Page<Activity> findByUserIdAndActivityType(UUID userId, String activityType, Pageable pageable);

    /**
     * Find all activities of a specific type for a user.
     *
     * @param userId The ID of the user
     * @param activityType The type of activity
     * @return List of activities of the specified type for the user
     */
    List<Activity> findByUserIdAndActivityType(UUID userId, String activityType);

    /**
     * Find all activities of a specific type for a garden plan with pagination.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param activityType The type of activity
     * @param pageable Pagination and sorting information
     * @return Page of activities of the specified type for the garden plan
     */
    Page<Activity> findByGardenPlanIdAndActivityType(UUID gardenPlanId, String activityType, Pageable pageable);

    /**
     * Find all activities of a specific type for a garden plan.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param activityType The type of activity
     * @return List of activities of the specified type for the garden plan
     */
    List<Activity> findByGardenPlanIdAndActivityType(UUID gardenPlanId, String activityType);
}
