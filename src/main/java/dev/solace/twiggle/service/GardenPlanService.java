package dev.solace.twiggle.service;

import dev.solace.twiggle.repository.GardenPlanRepository;
import dev.solace.twiggle.dto.GardenPlanDTO;
import dev.solace.twiggle.mapper.GardenPlanMapper;
import dev.solace.twiggle.model.GardenPlan;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * Find all garden plans.
     *
     * @return list of all garden plans as DTOs
     */
    public List<GardenPlanDTO> findAll() {
        return gardenPlanRepository.findAll().stream()
                .map(gardenPlanMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find garden plan by ID.
     *
     * @param id the garden plan ID
     * @return optional containing the garden plan DTO if found
     */
    public Optional<GardenPlanDTO> findById(UUID id) {
        return gardenPlanRepository.findById(id)
                .map(gardenPlanMapper::toDto);
    }

    /**
     * Find garden plans by user ID.
     *
     * @param userId the user ID
     * @return list of garden plan DTOs belonging to the user
     */
    public List<GardenPlanDTO> findByUserId(UUID userId) {
        return gardenPlanRepository.findByUserId(userId).stream()
                .map(gardenPlanMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find all public garden plans.
     *
     * @return list of public garden plan DTOs
     */
    public List<GardenPlanDTO> findPublicPlans() {
        return gardenPlanRepository.findByIsPublicTrue().stream()
                .map(gardenPlanMapper::toDto)
                .collect(Collectors.toList());
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
        return gardenPlanRepository.findById(id)
                .map(existingPlan -> {
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