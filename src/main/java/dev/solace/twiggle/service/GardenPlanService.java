package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.GardenPlanDTO;
import dev.solace.twiggle.mapper.GardenPlanMapper;
import dev.solace.twiggle.model.GardenPlan;
import dev.solace.twiggle.repository.GardenPlanRepository;
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
 * Service class for managing garden plans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GardenPlanService {

    private final GardenPlanRepository gardenPlanRepository;
    private final GardenPlanMapper gardenPlanMapper;

    /**
     * Find all garden plans with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of garden plan DTOs
     */
    public Page<GardenPlanDTO> findAll(Pageable pageable) {
        return gardenPlanRepository.findAll(pageable).map(gardenPlanMapper::toDto);
    }

    /**
     * Find all garden plans without pagination.
     *
     * @return list of all garden plans as DTOs
     */
    public List<GardenPlanDTO> findAll() {
        return gardenPlanRepository.findAll().stream()
                .map(gardenPlanMapper::toDto)
                .toList();
    }

    /**
     * Find garden plan by ID.
     *
     * @param id the garden plan ID
     * @return optional containing the garden plan DTO if found
     */
    public Optional<GardenPlanDTO> findById(UUID id) {
        return gardenPlanRepository.findById(id).map(gardenPlanMapper::toDto);
    }

    /**
     * Find garden plans by user ID with pagination and sorting.
     *
     * @param userId the user ID
     * @param pageable pagination and sorting parameters
     * @return page of garden plan DTOs belonging to the user
     */
    public Page<GardenPlanDTO> findByUserId(UUID userId, Pageable pageable) {
        return gardenPlanRepository.findByUserId(userId, pageable).map(gardenPlanMapper::toDto);
    }

    /**
     * Find garden plans by user ID without pagination.
     *
     * @param userId the user ID
     * @return list of garden plan DTOs belonging to the user
     */
    public List<GardenPlanDTO> findByUserId(UUID userId) {
        return gardenPlanRepository.findByUserId(userId).stream()
                .map(gardenPlanMapper::toDto)
                .toList();
    }

    /**
     * Find all public garden plans with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of public garden plan DTOs
     */
    public Page<GardenPlanDTO> findPublicPlans(Pageable pageable) {
        return gardenPlanRepository.findByIsPublicTrue(pageable).map(gardenPlanMapper::toDto);
    }

    /**
     * Find all public garden plans without pagination.
     *
     * @return list of public garden plan DTOs
     */
    public List<GardenPlanDTO> findPublicPlans() {
        return gardenPlanRepository.findByIsPublicTrue().stream()
                .map(gardenPlanMapper::toDto)
                .toList();
    }

    /**
     * Search garden plans by query, user ID, and public status.
     *
     * @param query search query for name, description, type, or location (optional)
     * @param userId filter by user ID (optional)
     * @param isPublic filter by public status (optional)
     * @param pageable pagination and sorting parameters
     * @return page of matching garden plan DTOs
     */
    public Page<GardenPlanDTO> searchGardenPlans(String query, UUID userId, Boolean isPublic, Pageable pageable) {
        return gardenPlanRepository
                .searchGardenPlans(query, userId, isPublic, pageable)
                .map(gardenPlanMapper::toDto);
    }

    /**
     * Enhanced search for garden plans with specific criteria for name, type, and location.
     * Returns results with the closest match to partial entries.
     *
     * @param name search term for garden plan name (optional)
     * @param type search term for garden plan type (optional)
     * @param location search term for garden plan location (optional)
     * @param query general search term for any field (optional)
     * @param userId filter by user ID (optional)
     * @param isPublic filter by public status (optional)
     * @param pageable pagination and sorting parameters
     * @return page of matching garden plan DTOs ordered by relevance
     */
    public Page<GardenPlanDTO> searchGardenPlansWithRelevance(
            String name, String type, String location, String query, UUID userId, Boolean isPublic, Pageable pageable) {
        try {
            return gardenPlanRepository
                    .searchGardenPlansWithRelevance(name, type, location, query, userId, isPublic, pageable)
                    .map(gardenPlanMapper::toDto);
        } catch (Exception e) {
            // If the enhanced search fails (e.g., due to database compatibility issues),
            // fall back to the simpler search method
            log.warn("Enhanced search failed, falling back to simple search: {}", e.getMessage());
            return gardenPlanRepository
                    .searchGardenPlans(query, userId, isPublic, pageable)
                    .map(gardenPlanMapper::toDto);
        }
    }

    /**
     * Create a new garden plan.
     *
     * @param gardenPlanDTO the garden plan DTO to create
     * @return the created garden plan DTO
     */
    @Transactional
    public GardenPlanDTO create(GardenPlanDTO gardenPlanDTO) {
        OffsetDateTime now = OffsetDateTime.now();
        gardenPlanDTO.setCreatedAt(now);
        gardenPlanDTO.setUpdatedAt(now);

        GardenPlan gardenPlan = gardenPlanMapper.toEntity(gardenPlanDTO);
        GardenPlan savedGardenPlan = gardenPlanRepository.save(gardenPlan);

        return gardenPlanMapper.toDto(savedGardenPlan);
    }

    /**
     * Update an existing garden plan.
     *
     * @param id the garden plan ID
     * @param gardenPlanDTO the updated garden plan details
     * @return the updated garden plan DTO if found
     */
    @Transactional
    public Optional<GardenPlanDTO> update(UUID id, GardenPlanDTO gardenPlanDTO) {
        return gardenPlanRepository.findById(id).map(existingPlan -> {
            // Update fields from the DTO
            existingPlan.setName(gardenPlanDTO.getName());
            existingPlan.setType(gardenPlanDTO.getType());
            existingPlan.setDescription(gardenPlanDTO.getDescription());
            existingPlan.setLocation(gardenPlanDTO.getLocation());
            existingPlan.setThumbnailUrl(gardenPlanDTO.getThumbnailUrl());
            existingPlan.setIsPublic(gardenPlanDTO.getIsPublic());
            existingPlan.setUpdatedAt(OffsetDateTime.now());

            // Save and convert back to DTO
            return gardenPlanMapper.toDto(gardenPlanRepository.save(existingPlan));
        });
    }

    /**
     * Delete a garden plan by ID.
     *
     * @param id the garden plan ID
     */
    @Transactional
    public void delete(UUID id) {
        gardenPlanRepository.deleteById(id);
    }
}
