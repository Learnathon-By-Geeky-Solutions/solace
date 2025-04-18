package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.PlantReminderDTO;
import dev.solace.twiggle.mapper.PlantReminderMapper;
import dev.solace.twiggle.model.PlantReminder;
import dev.solace.twiggle.repository.PlantReminderRepository;
import java.time.LocalDate;
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
 * Service class for managing plant reminders.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlantReminderService {

    private final PlantReminderRepository plantReminderRepository;
    private final PlantReminderMapper plantReminderMapper;

    /**
     * Find all plant reminders with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of plant reminder DTOs
     */
    public Page<PlantReminderDTO> findAll(Pageable pageable) {
        return plantReminderRepository.findAll(pageable).map(plantReminderMapper::toDto);
    }

    /**
     * Find all plant reminders without pagination.
     *
     * @return list of all plant reminder DTOs
     */
    public List<PlantReminderDTO> findAll() {
        return plantReminderRepository.findAll().stream()
                .map(plantReminderMapper::toDto)
                .toList();
    }

    /**
     * Find plant reminder by ID.
     *
     * @param id the plant reminder ID
     * @return optional containing the plant reminder DTO if found
     */
    public Optional<PlantReminderDTO> findById(UUID id) {
        return plantReminderRepository.findById(id).map(plantReminderMapper::toDto);
    }

    /**
     * Find plant reminders by plant ID with pagination.
     *
     * @param plantId the plant ID
     * @param pageable pagination and sorting parameters
     * @return page of plant reminder DTOs for the plant
     */
    public Page<PlantReminderDTO> findByPlantId(UUID plantId, Pageable pageable) {
        return plantReminderRepository.findByPlantId(plantId, pageable).map(plantReminderMapper::toDto);
    }

    /**
     * Find plant reminders by plant ID without pagination.
     *
     * @param plantId the plant ID
     * @return list of plant reminder DTOs for the plant
     */
    public List<PlantReminderDTO> findByPlantId(UUID plantId) {
        return plantReminderRepository.findByPlantId(plantId).stream()
                .map(plantReminderMapper::toDto)
                .toList();
    }

    /**
     * Find plant reminders by garden plan ID with pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @param pageable pagination and sorting parameters
     * @return page of plant reminder DTOs for the garden plan
     */
    public Page<PlantReminderDTO> findByGardenPlanId(UUID gardenPlanId, Pageable pageable) {
        return plantReminderRepository
                .findByGardenPlanId(gardenPlanId, pageable)
                .map(plantReminderMapper::toDto);
    }

    /**
     * Find plant reminders by garden plan ID without pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @return list of plant reminder DTOs for the garden plan
     */
    public List<PlantReminderDTO> findByGardenPlanId(UUID gardenPlanId) {
        return plantReminderRepository.findByGardenPlanId(gardenPlanId).stream()
                .map(plantReminderMapper::toDto)
                .toList();
    }

    /**
     * Find plant reminders by completion status for a specific plant.
     *
     * @param plantId the plant ID
     * @param isCompleted the completion status (true for completed, false for incomplete)
     * @return list of plant reminder DTOs with the specified completion status
     */
    public List<PlantReminderDTO> findByPlantIdAndIsCompleted(UUID plantId, Boolean isCompleted) {
        return plantReminderRepository.findByPlantIdAndIsCompleted(plantId, isCompleted).stream()
                .map(plantReminderMapper::toDto)
                .toList();
    }

    /**
     * Find plant reminders due on or before a specific date with pagination.
     *
     * @param date the date to check against
     * @param pageable pagination and sorting parameters
     * @return page of plant reminder DTOs due on or before the specified date
     */
    public Page<PlantReminderDTO> findByReminderDateLessThanEqual(LocalDate date, Pageable pageable) {
        return plantReminderRepository
                .findByReminderDateLessThanEqual(date, pageable)
                .map(plantReminderMapper::toDto);
    }

    /**
     * Create a new plant reminder.
     *
     * @param reminderDTO the plant reminder DTO to create
     * @return the created plant reminder DTO
     */
    @Transactional
    public PlantReminderDTO create(PlantReminderDTO reminderDTO) {
        reminderDTO.setCreatedAt(OffsetDateTime.now());
        if (reminderDTO.getIsCompleted() == null) {
            reminderDTO.setIsCompleted(false);
        }

        PlantReminder reminder = plantReminderMapper.toEntity(reminderDTO);
        PlantReminder savedReminder = plantReminderRepository.save(reminder);

        return plantReminderMapper.toDto(savedReminder);
    }

    /**
     * Update an existing plant reminder.
     *
     * @param id the plant reminder ID
     * @param reminderDTO the updated plant reminder details
     * @return the updated plant reminder DTO if found
     */
    @Transactional
    public Optional<PlantReminderDTO> update(UUID id, PlantReminderDTO reminderDTO) {
        return plantReminderRepository.findById(id).map(existingReminder -> {
            // Update fields from the DTO
            existingReminder.setPlantId(reminderDTO.getPlantId());
            existingReminder.setGardenPlanId(reminderDTO.getGardenPlanId());
            existingReminder.setReminderType(reminderDTO.getReminderType());
            existingReminder.setReminderDate(reminderDTO.getReminderDate());
            existingReminder.setNotes(reminderDTO.getNotes());
            existingReminder.setIsCompleted(reminderDTO.getIsCompleted());

            // Save and convert back to DTO
            return plantReminderMapper.toDto(plantReminderRepository.save(existingReminder));
        });
    }

    /**
     * Mark a plant reminder as completed.
     *
     * @param id the plant reminder ID
     * @return the updated plant reminder DTO if found
     */
    @Transactional
    public Optional<PlantReminderDTO> markAsCompleted(UUID id) {
        return plantReminderRepository.findById(id).map(existingReminder -> {
            existingReminder.setIsCompleted(true);
            return plantReminderMapper.toDto(plantReminderRepository.save(existingReminder));
        });
    }

    /**
     * Delete a plant reminder by ID.
     *
     * @param id the plant reminder ID
     */
    @Transactional
    public void delete(UUID id) {
        plantReminderRepository.deleteById(id);
    }
}
