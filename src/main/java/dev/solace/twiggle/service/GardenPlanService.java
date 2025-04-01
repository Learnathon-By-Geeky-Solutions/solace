package dev.solace.twiggle.service;

import dev.solace.twiggle.model.postgres.GardenPlan;
import dev.solace.twiggle.repository.postgres.GardenPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing garden plans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GardenPlanService {

    private final GardenPlanRepository gardenPlanRepository;

    /**
     * Find all garden plans.
     *
     * @return list of all garden plans
     */
    public List<GardenPlan> findAll() {
        return gardenPlanRepository.findAll();
    }

    /**
     * Find garden plan by ID.
     *
     * @param id the garden plan ID
     * @return optional containing the garden plan if found
     */
    public Optional<GardenPlan> findById(UUID id) {
        return gardenPlanRepository.findById(id);
    }

    /**
     * Find garden plans by user ID.
     *
     * @param userId the user ID
     * @return list of garden plans belonging to the user
     */
    public List<GardenPlan> findByUserId(UUID userId) {
        return gardenPlanRepository.findByUserId(userId);
    }

    /**
     * Find all public garden plans.
     *
     * @return list of public garden plans
     */
    public List<GardenPlan> findPublicPlans() {
        return gardenPlanRepository.findByIsPublicTrue();
    }

    /**
     * Create a new garden plan.
     *
     * @param gardenPlan the garden plan to create
     * @return the created garden plan
     */
    @Transactional
    public GardenPlan create(GardenPlan gardenPlan) {
        OffsetDateTime now = OffsetDateTime.now();
        gardenPlan.setCreatedAt(now);
        gardenPlan.setUpdatedAt(now);
        return gardenPlanRepository.save(gardenPlan);
    }

    /**
     * Update an existing garden plan.
     *
     * @param id the garden plan ID
     * @param gardenPlan the updated garden plan details
     * @return the updated garden plan
     */
    @Transactional
    public Optional<GardenPlan> update(UUID id, GardenPlan gardenPlan) {
        return gardenPlanRepository.findById(id)
                .map(existingPlan -> {
                    existingPlan.setName(gardenPlan.getName());
                    existingPlan.setType(gardenPlan.getType());
                    existingPlan.setDescription(gardenPlan.getDescription());
                    existingPlan.setLocation(gardenPlan.getLocation());
                    existingPlan.setThumbnailUrl(gardenPlan.getThumbnailUrl());
                    existingPlan.setIsPublic(gardenPlan.getIsPublic());
                    existingPlan.setUpdatedAt(OffsetDateTime.now());
                    return gardenPlanRepository.save(existingPlan);
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