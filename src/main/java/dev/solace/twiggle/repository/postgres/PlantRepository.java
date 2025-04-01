package dev.solace.twiggle.repository.postgres;

import dev.solace.twiggle.model.postgres.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for the Plant entity.
 */
@Repository
public interface PlantRepository extends JpaRepository<Plant, UUID> {
    
    /**
     * Find all plants belonging to a specific garden plan.
     *
     * @param gardenPlanId the garden plan ID
     * @return list of plants
     */
    List<Plant> findByGardenPlanId(UUID gardenPlanId);
    
    /**
     * Find plants by type.
     *
     * @param type the plant type
     * @return list of plants
     */
    List<Plant> findByType(String type);
} 