package dev.solace.twiggle.controller;

import dev.solace.twiggle.model.postgres.GardenPlan;
import dev.solace.twiggle.service.GardenPlanService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for garden plans.
 */
@RestController
@RequestMapping("/api/garden-plans")
@RequiredArgsConstructor
public class GardenPlanController {

    private final GardenPlanService gardenPlanService;

    /**
     * Get all garden plans.
     *
     * @return list of garden plans
     */
    @GetMapping
    @RateLimiter(name = "standard-api")
    public ResponseEntity<List<GardenPlan>> getAllGardenPlans() {
        return ResponseEntity.ok(gardenPlanService.findAll());
    }

    /**
     * Get a garden plan by ID.
     *
     * @param id the garden plan ID
     * @return the garden plan if found
     */
    @GetMapping("/{id}")
    @RateLimiter(name = "standard-api")
    public ResponseEntity<GardenPlan> getGardenPlanById(@PathVariable UUID id) {
        return gardenPlanService
                .findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get garden plans by user ID.
     *
     * @param userId the user ID
     * @return list of garden plans
     */
    @GetMapping("/user/{userId}")
    @RateLimiter(name = "standard-api")
    public ResponseEntity<List<GardenPlan>> getGardenPlansByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(gardenPlanService.findByUserId(userId));
    }

    /**
     * Get public garden plans.
     *
     * @return list of public garden plans
     */
    @GetMapping("/public")
    @RateLimiter(name = "standard-api")
    public ResponseEntity<List<GardenPlan>> getPublicGardenPlans() {
        return ResponseEntity.ok(gardenPlanService.findPublicPlans());
    }

    /**
     * Create a new garden plan.
     *
     * @param gardenPlan the garden plan to create
     * @return the created garden plan
     */
    @PostMapping
    @RateLimiter(name = "standard-api")
    public ResponseEntity<GardenPlan> createGardenPlan(
            @RequestBody GardenPlan gardenPlan, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        GardenPlan createdPlan = gardenPlanService.create(gardenPlan);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlan);
    }

    /**
     * Update an existing garden plan.
     *
     * @param id the garden plan ID
     * @param gardenPlan the updated garden plan
     * @return the updated garden plan
     */
    @PutMapping("/{id}")
    @RateLimiter(name = "standard-api")
    public ResponseEntity<GardenPlan> updateGardenPlan(@PathVariable UUID id, @RequestBody GardenPlan gardenPlan) {
        if (gardenPlan.getId() != null && !id.equals(gardenPlan.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return gardenPlanService
                .update(id, gardenPlan)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a garden plan.
     *
     * @param id the garden plan ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @RateLimiter(name = "standard-api")
    public ResponseEntity<Void> deleteGardenPlan(@PathVariable UUID id) {
        try {
            gardenPlanService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
