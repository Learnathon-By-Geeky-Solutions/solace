package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.mapper.PlantMapper;
import dev.solace.twiggle.model.Plant;
import dev.solace.twiggle.repository.PlantRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing plants.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlantService {

    private final PlantRepository plantRepository;
    private final PlantMapper plantMapper;

    /**
     * Find all plants with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of plant DTOs
     */
    public Page<PlantDTO> findAll(Pageable pageable) {
        return plantRepository.findAll(pageable).map(plantMapper::toDto);
    }

    /**
     * Find all plants without pagination.
     *
     * @return list of all plant DTOs
     */
    public List<PlantDTO> findAll() {
        return plantRepository.findAll().stream().map(plantMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Find plant by ID.
     *
     * @param id the plant ID
     * @return optional containing the plant DTO if found
     */
    public Optional<PlantDTO> findById(UUID id) {
        return plantRepository.findById(id).map(plantMapper::toDto);
    }

    /**
     * Find plants by garden plan ID with pagination and sorting.
     *
     * @param gardenPlanId the garden plan ID
     * @param pageable pagination and sorting parameters
     * @return page of plant DTOs in the garden plan
     */
    public Page<PlantDTO> findByGardenPlanId(UUID gardenPlanId, Pageable pageable) {
        return plantRepository.findByGardenPlanId(gardenPlanId, pageable).map(plantMapper::toDto);
    }

    /**
     * Find plants by garden plan ID without pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @return list of plant DTOs in the garden plan
     */
    public List<PlantDTO> findByGardenPlanId(UUID gardenPlanId) {
        return plantRepository.findByGardenPlanId(gardenPlanId).stream()
                .map(plantMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find plants by type with pagination and sorting.
     *
     * @param type the plant type
     * @param pageable pagination and sorting parameters
     * @return page of plant DTOs of the specified type
     */
    public Page<PlantDTO> findByType(String type, Pageable pageable) {
        return plantRepository.findByType(type, pageable).map(plantMapper::toDto);
    }

    /**
     * Find plants by type without pagination.
     *
     * @param type the plant type
     * @return list of plant DTOs of the specified type
     */
    public List<PlantDTO> findByType(String type) {
        return plantRepository.findByType(type).stream().map(plantMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Search plants by query and garden plan ID.
     *
     * @param query search query for name, description, type, or watering frequency (optional)
     * @param gardenPlanId filter by garden plan ID (optional)
     * @param pageable pagination and sorting parameters
     * @return page of matching plant DTOs
     */
    public Page<PlantDTO> searchPlants(String query, UUID gardenPlanId, Pageable pageable) {
        return plantRepository.searchPlants(query, gardenPlanId, pageable).map(plantMapper::toDto);
    }

    /**
     * Enhanced search for plants with specific criteria and relevance scoring.
     *
     * @param name search term for plant name (optional)
     * @param type search term for plant type (optional)
     * @param wateringFrequency search term for watering frequency (optional)
     * @param sunlightRequirements search term for sunlight requirements (optional)
     * @param query general search term for any field (optional)
     * @param gardenPlanId filter by garden plan ID (optional)
     * @param pageable pagination and sorting parameters
     * @return page of matching plant DTOs ordered by relevance
     */
    public Page<PlantDTO> searchPlantsWithRelevance(
            String name,
            String type,
            String wateringFrequency,
            String sunlightRequirements,
            String query,
            UUID gardenPlanId,
            Pageable pageable) {
        try {
            return plantRepository
                    .searchPlantsWithRelevance(
                            name, type, wateringFrequency, sunlightRequirements, query, gardenPlanId, pageable)
                    .map(plantMapper::toDto);
        } catch (Exception e) {
            // If the enhanced search fails, fall back to the simpler search method
            log.warn("Enhanced plant search failed, falling back to simple search: {}", e.getMessage());
            return plantRepository.searchPlants(query, gardenPlanId, pageable).map(plantMapper::toDto);
        }
    }

    /**
     * Create a new plant.
     *
     * @param plantDTO the plant DTO to create
     * @return the created plant DTO
     */
    @Transactional
    public PlantDTO create(PlantDTO plantDTO) {
        OffsetDateTime now = OffsetDateTime.now();
        plantDTO.setCreatedAt(now);
        plantDTO.setUpdatedAt(now);

        Plant plant = plantMapper.toEntity(plantDTO);
        Plant savedPlant = plantRepository.save(plant);

        return plantMapper.toDto(savedPlant);
    }

    /**
     * Update an existing plant.
     *
     * @param id the plant ID
     * @param plantDTO the updated plant details
     * @return the updated plant DTO if found
     */
    @Transactional
    public Optional<PlantDTO> update(UUID id, PlantDTO plantDTO) {
        return plantRepository.findById(id).map(existingPlant -> {
            // Update fields from the DTO
            existingPlant.setName(plantDTO.getName());
            existingPlant.setType(plantDTO.getType());
            existingPlant.setDescription(plantDTO.getDescription());
            existingPlant.setWateringFrequency(plantDTO.getWateringFrequency());
            existingPlant.setSunlightRequirements(plantDTO.getSunlightRequirements());
            existingPlant.setPositionX(plantDTO.getPositionX());
            existingPlant.setPositionY(plantDTO.getPositionY());
            existingPlant.setImageUrl(plantDTO.getImageUrl());
            existingPlant.setUpdatedAt(OffsetDateTime.now());

            // Save and convert back to DTO
            return plantMapper.toDto(plantRepository.save(existingPlant));
        });
    }

    /**
     * Delete a plant by ID.
     *
     * @param id the plant ID
     */
    @Transactional
    public void delete(UUID id) {
        plantRepository.deleteById(id);
    }
}
