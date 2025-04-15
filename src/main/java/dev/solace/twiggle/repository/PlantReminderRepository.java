package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.PlantReminder;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing PlantReminder entities.
 */
@Repository
public interface PlantReminderRepository extends JpaRepository<PlantReminder, UUID> {

    /**
     * Find all reminders for a specific plant with pagination.
     *
     * @param plantId The ID of the plant
     * @param pageable Pagination and sorting information
     * @return Page of reminders for the plant
     */
    Page<PlantReminder> findByPlantId(UUID plantId, Pageable pageable);

    /**
     * Find all reminders for a specific plant.
     *
     * @param plantId The ID of the plant
     * @return List of reminders for the plant
     */
    List<PlantReminder> findByPlantId(UUID plantId);

    /**
     * Find all reminders for a specific garden plan with pagination.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param pageable Pagination and sorting information
     * @return Page of reminders for the garden plan
     */
    Page<PlantReminder> findByGardenPlanId(UUID gardenPlanId, Pageable pageable);

    /**
     * Find all reminders for a specific garden plan.
     *
     * @param gardenPlanId The ID of the garden plan
     * @return List of reminders for the garden plan
     */
    List<PlantReminder> findByGardenPlanId(UUID gardenPlanId);

    /**
     * Find all reminders due on or before a specific date with pagination.
     *
     * @param date The due date
     * @param pageable Pagination and sorting information
     * @return Page of reminders due on or before the specified date
     */
    Page<PlantReminder> findByReminderDateLessThanEqual(LocalDate date, Pageable pageable);

    /**
     * Find all reminders due on or before a specific date.
     *
     * @param date The due date
     * @return List of reminders due on or before the specified date
     */
    List<PlantReminder> findByReminderDateLessThanEqual(LocalDate date);

    /**
     * Find all incomplete reminders for a specific plant.
     *
     * @param plantId The ID of the plant
     * @param isCompleted The completion status (false for incomplete)
     * @return List of incomplete reminders for the plant
     */
    List<PlantReminder> findByPlantIdAndIsCompleted(UUID plantId, Boolean isCompleted);

    /**
     * Find all reminders of a specific type for a garden plan.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param reminderType The type of reminder
     * @return List of reminders of the specified type for the garden plan
     */
    List<PlantReminder> findByGardenPlanIdAndReminderType(UUID gardenPlanId, String reminderType);

    /**
     * Find all reminders of a specific type for a garden plan with pagination.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param reminderType The type of reminder
     * @param pageable Pagination and sorting information
     * @return Page of reminders of the specified type for the garden plan
     */
    Page<PlantReminder> findByGardenPlanIdAndReminderType(UUID gardenPlanId, String reminderType, Pageable pageable);

    /**
     * Find all incomplete reminders for a specific plant with pagination.
     *
     * @param plantId The ID of the plant
     * @param isCompleted The completion status (false for incomplete)
     * @param pageable Pagination and sorting information
     * @return Page of incomplete reminders for the plant
     */
    Page<PlantReminder> findByPlantIdAndIsCompleted(UUID plantId, Boolean isCompleted, Pageable pageable);
}
