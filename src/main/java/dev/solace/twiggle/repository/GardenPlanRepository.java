package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.GardenPlan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the GardenPlan entity.
 */
@Repository
public interface GardenPlanRepository extends JpaRepository<GardenPlan, UUID> {

    /**
     * Find all garden plans belonging to a specific user with pagination and sorting.
     *
     * @param userId the user ID
     * @param pageable pagination and sorting parameters
     * @return page of garden plans
     */
    Page<GardenPlan> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all garden plans belonging to a specific user without pagination.
     *
     * @param userId the user ID
     * @return list of garden plans
     */
    List<GardenPlan> findByUserId(UUID userId);

    /**
     * Find all public garden plans with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of public garden plans
     */
    Page<GardenPlan> findByIsPublicTrue(Pageable pageable);

    /**
     * Find all public garden plans without pagination.
     *
     * @return list of public garden plans
     */
    List<GardenPlan> findByIsPublicTrue();
}
