package dev.solace.twiggle.repository.postgres;

import dev.solace.twiggle.model.postgres.GardenPlan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the GardenPlan entity.
 */
@Repository
public interface GardenPlanRepository extends JpaRepository<GardenPlan, UUID> {

    /**
     * Find all garden plans belonging to a specific user.
     *
     * @param userId the user ID
     * @return list of garden plans
     */
    List<GardenPlan> findByUserId(UUID userId);

    /**
     * Find all public garden plans.
     *
     * @return list of public garden plans
     */
    List<GardenPlan> findByIsPublicTrue();
}
