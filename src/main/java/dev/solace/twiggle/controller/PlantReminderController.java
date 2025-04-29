package dev.solace.twiggle.controller;

import dev.solace.twiggle.dto.ApiResponse;
import dev.solace.twiggle.dto.PlantReminderDTO;
import dev.solace.twiggle.exception.CustomException;
import dev.solace.twiggle.exception.ErrorCode;
import dev.solace.twiggle.service.PlantReminderService;
import dev.solace.twiggle.util.ResponseUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import java.time.LocalDate;
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
 * REST controller for plant reminders.
 */
@RestController
@RequestMapping("/api/plant-reminders")
@RequiredArgsConstructor
@Slf4j
@RateLimiter(name = "standard-api")
public class PlantReminderController {

    private static final String PLANT_REMINDER_NOT_FOUND = "Plant reminder not found";
    private final PlantReminderService plantReminderService;

    /**
     * Get all plant reminders with pagination and sorting.
     *
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant reminder DTOs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PlantReminderDTO>>> getAllPlantReminders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reminderDate") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<PlantReminderDTO> reminders = plantReminderService.findAll(pageable);
        return ResponseUtil.success("Successfully retrieved plant reminders", reminders);
    }

    /**
     * Get all plant reminders without pagination (for backward compatibility).
     *
     * @return list of all plant reminder DTOs
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PlantReminderDTO>>> getAllPlantRemindersWithoutPagination() {
        List<PlantReminderDTO> reminders = plantReminderService.findAll();
        return ResponseUtil.success("Successfully retrieved all plant reminders", reminders);
    }

    /**
     * Get a plant reminder by ID.
     *
     * @param id the plant reminder ID
     * @return the plant reminder DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantReminderDTO>> getPlantReminderById(@PathVariable UUID id) {
        return plantReminderService
                .findById(id)
                .map(reminder -> ResponseUtil.success("Successfully retrieved plant reminder", reminder))
                .orElseThrow(() -> new CustomException(
                        PLANT_REMINDER_NOT_FOUND, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Get plant reminders by plant ID with pagination and sorting.
     *
     * @param plantId   the plant ID
     * @param page      page number (0-based)
     * @param size      page size
     * @param sort      sort property
     * @param direction sort direction (ASC or DESC)
     * @return page of plant reminder DTOs
     */
    @GetMapping("/plant/{plantId}")
    public ResponseEntity<ApiResponse<Page<PlantReminderDTO>>> getRemindersByPlantId(
            @PathVariable UUID plantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reminderDate") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<PlantReminderDTO> reminders = plantReminderService.findByPlantId(plantId, pageable);
        return ResponseUtil.success("Successfully retrieved reminders for plant", reminders);
    }

    /**
     * Get plant reminders by garden plan ID with pagination and sorting.
     *
     * @param gardenPlanId the garden plan ID
     * @param page         page number (0-based)
     * @param size         page size
     * @param sort         sort property
     * @param direction    sort direction (ASC or DESC)
     * @return page of plant reminder DTOs
     */
    @GetMapping("/garden-plan/{gardenPlanId}")
    public ResponseEntity<ApiResponse<Page<PlantReminderDTO>>> getRemindersByGardenPlanId(
            @PathVariable UUID gardenPlanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reminderDate") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        Page<PlantReminderDTO> reminders = plantReminderService.findByGardenPlanId(gardenPlanId, pageable);
        return ResponseUtil.success("Successfully retrieved reminders for garden plan", reminders);
    }

    /**
     * Get incomplete reminders for a plant.
     *
     * @param plantId the plant ID
     * @return list of incomplete reminder DTOs
     */
    @GetMapping("/plant/{plantId}/incomplete")
    public ResponseEntity<ApiResponse<List<PlantReminderDTO>>> getIncompleteRemindersByPlantId(
            @PathVariable UUID plantId) {
        List<PlantReminderDTO> reminders = plantReminderService.findByPlantIdAndIsCompleted(plantId, false);
        return ResponseUtil.success("Successfully retrieved incomplete reminders for plant", reminders);
    }

    /**
     * Get reminders due on or before a specific date.
     *
     * @param date the date to check against
     * @param page page number (0-based)
     * @param size page size
     * @return page of due reminder DTOs
     */
    @GetMapping("/due")
    public ResponseEntity<ApiResponse<Page<PlantReminderDTO>>> getRemindersDueByDate(
            @RequestParam LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "reminderDate"));
        Page<PlantReminderDTO> reminders = plantReminderService.findByReminderDateLessThanEqual(date, pageable);
        return ResponseUtil.success("Successfully retrieved due reminders", reminders);
    }

    /**
     * Create a new plant reminder.
     *
     * @param reminderDTO the reminder DTO to create (validated)
     * @return the created reminder DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PlantReminderDTO>> createPlantReminder(
            @Valid @RequestBody PlantReminderDTO reminderDTO) {
        PlantReminderDTO createdReminder = plantReminderService.create(reminderDTO);
        return ResponseUtil.success("Plant reminder created successfully", createdReminder);
    }

    /**
     * Update an existing plant reminder.
     *
     * @param id          the reminder ID
     * @param reminderDTO the updated reminder DTO (validated)
     * @return the updated reminder DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlantReminderDTO>> updatePlantReminder(
            @PathVariable UUID id, @Valid @RequestBody PlantReminderDTO reminderDTO) {
        return plantReminderService
                .update(id, reminderDTO)
                .map(updatedReminder -> ResponseUtil.success("Plant reminder updated successfully", updatedReminder))
                .orElseThrow(() -> new CustomException(
                        PLANT_REMINDER_NOT_FOUND, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Mark a plant reminder as completed.
     *
     * @param id the reminder ID
     * @return the updated reminder DTO
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PlantReminderDTO>> markReminderAsCompleted(@PathVariable UUID id) {
        return plantReminderService
                .markAsCompleted(id)
                .map(updatedReminder -> ResponseUtil.success("Plant reminder marked as completed", updatedReminder))
                .orElseThrow(() -> new CustomException(
                        PLANT_REMINDER_NOT_FOUND, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * Delete a plant reminder.
     *
     * @param id the reminder ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlantReminder(@PathVariable UUID id) {
        plantReminderService.delete(id);
        return ResponseUtil.success("Plant reminder deleted successfully", null);
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
