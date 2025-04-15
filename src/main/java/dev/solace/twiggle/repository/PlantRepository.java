package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.Plant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Plant entity.
 */
@Repository
public interface PlantRepository extends JpaRepository<Plant, UUID> {

    /**
     * Find all plants belonging to a specific garden plan with pagination and sorting.
     *
     * @param gardenPlanId the garden plan ID
     * @param pageable pagination and sorting parameters
     * @return page of plants
     */
    Page<Plant> findByGardenPlanId(UUID gardenPlanId, Pageable pageable);

    /**
     * Find all plants belonging to a specific garden plan without pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @return list of plants
     */
    List<Plant> findByGardenPlanId(UUID gardenPlanId);

    /**
     * Find plants by type with pagination and sorting.
     *
     * @param type the plant type
     * @param pageable pagination and sorting parameters
     * @return page of plants
     */
    Page<Plant> findByType(String type, Pageable pageable);

    /**
     * Find plants by type without pagination.
     *
     * @param type the plant type
     * @return list of plants
     */
    List<Plant> findByType(String type);

    /**
     * Search plants by various criteria.
     *
     * @param query search term for name, description, type, or watering frequency
     * @param gardenPlanId optional garden plan ID to filter by (can be null)
     * @param pageable pagination and sorting parameters
     * @return page of matching plants
     */
    @Query("SELECT p FROM Plant p WHERE " + "(:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(p.type) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(p.wateringFrequency) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(p.sunlightRequirements) LIKE LOWER(CONCAT('%', :query, '%'))) "
            + "AND (:gardenPlanId IS NULL OR p.gardenPlanId = :gardenPlanId)")
    Page<Plant> searchPlants(@Param("query") String query, @Param("gardenPlanId") UUID gardenPlanId, Pageable pageable);

    /**
     * Enhanced search for plants with relevance scoring.
     * The search prioritizes name matches, then type, and finally other fields.
     * It will return closest matching results when partial entries are provided.
     *
     * @param name search term for name (can be null)
     * @param type search term for type (can be null)
     * @param wateringFrequency search term for watering frequency (can be null)
     * @param sunlightRequirements search term for sunlight requirements (can be null)
     * @param query general search term for any field including description (can be null)
     * @param gardenPlanId optional garden plan ID to filter by (can be null)
     * @param pageable pagination and sorting parameters
     * @return page of matching plants ordered by relevance
     */
    @Query(
            value = "SELECT p.*, " + "CASE "
                    + "  WHEN LOWER(p.name) = LOWER(:name) THEN 100 "
                    + "  WHEN LOWER(p.name) LIKE LOWER(CONCAT(:name, '%')) THEN 80 "
                    + "  WHEN LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) THEN 60 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN LOWER(p.type) = LOWER(:type) THEN 50 "
                    + "  WHEN LOWER(p.type) LIKE LOWER(CONCAT(:type, '%')) THEN 40 "
                    + "  WHEN LOWER(p.type) LIKE LOWER(CONCAT('%', :type, '%')) THEN 30 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN LOWER(p.watering_frequency) = LOWER(:wateringFrequency) THEN 25 "
                    + "  WHEN LOWER(p.watering_frequency) LIKE LOWER(CONCAT(:wateringFrequency, '%')) THEN 20 "
                    + "  WHEN LOWER(p.watering_frequency) LIKE LOWER(CONCAT('%', :wateringFrequency, '%')) THEN 15 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN LOWER(p.sunlight_requirements) = LOWER(:sunlightRequirements) THEN 25 "
                    + "  WHEN LOWER(p.sunlight_requirements) LIKE LOWER(CONCAT(:sunlightRequirements, '%')) THEN 20 "
                    + "  WHEN LOWER(p.sunlight_requirements) LIKE LOWER(CONCAT('%', :sunlightRequirements, '%')) THEN 15 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN (:query IS NOT NULL) AND ("
                    + "       LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "       LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "       LOWER(p.type) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "       LOWER(p.watering_frequency) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "       LOWER(p.sunlight_requirements) LIKE LOWER(CONCAT('%', :query, '%'))) THEN 10 "
                    + "  ELSE 0 "
                    + "END AS relevance_score "
                    + "FROM plants p "
                    + "WHERE (:name IS NULL OR "
                    + "       LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
                    + "AND (:type IS NULL OR "
                    + "     LOWER(p.type) LIKE LOWER(CONCAT('%', :type, '%'))) "
                    + "AND (:wateringFrequency IS NULL OR "
                    + "     LOWER(p.watering_frequency) LIKE LOWER(CONCAT('%', :wateringFrequency, '%'))) "
                    + "AND (:sunlightRequirements IS NULL OR "
                    + "     LOWER(p.sunlight_requirements) LIKE LOWER(CONCAT('%', :sunlightRequirements, '%'))) "
                    + "AND (:query IS NULL OR "
                    + "     LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "     LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "     LOWER(p.type) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "     LOWER(p.watering_frequency) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "     LOWER(p.sunlight_requirements) LIKE LOWER(CONCAT('%', :query, '%'))) "
                    + "AND (:gardenPlanId IS NULL OR p.garden_plan_id = :gardenPlanId) "
                    + "ORDER BY relevance_score DESC",
            nativeQuery = true)
    Page<Plant> searchPlantsWithRelevance(
            @Param("name") String name,
            @Param("type") String type,
            @Param("wateringFrequency") String wateringFrequency,
            @Param("sunlightRequirements") String sunlightRequirements,
            @Param("query") String query,
            @Param("gardenPlanId") UUID gardenPlanId,
            Pageable pageable);
}
