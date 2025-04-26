package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.GardenPlan;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    Page<GardenPlan> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all garden plans belonging to a specific user without pagination.
     *
     * @param userId the user ID
     * @return list of garden plans
     */
    @Transactional(readOnly = true)
    List<GardenPlan> findByUserId(UUID userId);

    /**
     * Find all public garden plans with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of public garden plans
     */
    @Transactional(readOnly = true)
    Page<GardenPlan> findByIsPublicTrue(Pageable pageable);

    /**
     * Find all public garden plans without pagination.
     *
     * @return list of public garden plans
     */
    @Transactional(readOnly = true)
    List<GardenPlan> findByIsPublicTrue();

    /**
     * Enhanced search for garden plans by various criteria with relevance scoring.
     * The search prioritizes name matches, then type, and finally location.
     * It will return closest matching results when partial entries are provided.
     *
     * @param name search term for name (can be null)
     * @param type search term for type (can be null)
     * @param location search term for location (can be null)
     * @param query general search term for any field including description (can be null)
     * @param userId optional user ID to filter by (can be null)
     * @param isPublic optional flag to filter by public status (can be null)
     * @param pageable pagination and sorting parameters
     * @return page of matching garden plans ordered by relevance
     */
    @Transactional(readOnly = true)
    @Query(
            value = "SELECT g.*, " + "CASE "
                    + "  WHEN LOWER(g.name) = LOWER(:name) THEN 100 "
                    + "  WHEN LOWER(g.name) LIKE LOWER(CONCAT(:name, '%')) THEN 80 "
                    + "  WHEN LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')) THEN 60 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN LOWER(g.type) = LOWER(:type) THEN 50 "
                    + "  WHEN LOWER(g.type) LIKE LOWER(CONCAT(:type, '%')) THEN 40 "
                    + "  WHEN LOWER(g.type) LIKE LOWER(CONCAT('%', :type, '%')) THEN 30 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN LOWER(g.location) = LOWER(:location) THEN 25 "
                    + "  WHEN LOWER(g.location) LIKE LOWER(CONCAT(:location, '%')) THEN 20 "
                    + "  WHEN LOWER(g.location) LIKE LOWER(CONCAT('%', :location, '%')) THEN 15 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN (:query IS NOT NULL) AND ("
                    + "       LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "       LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "       LOWER(g.type) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "       LOWER(g.location) LIKE LOWER(CONCAT('%', :query, '%'))) THEN 10 "
                    + "  ELSE 0 "
                    + "END AS relevance_score "
                    + "FROM garden_plans g "
                    + "WHERE (:name IS NULL OR "
                    + "       LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
                    + "AND (:type IS NULL OR "
                    + "     LOWER(g.type) LIKE LOWER(CONCAT('%', :type, '%'))) "
                    + "AND (:location IS NULL OR "
                    + "     LOWER(g.location) LIKE LOWER(CONCAT('%', :location, '%'))) "
                    + "AND (:query IS NULL OR "
                    + "     LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "     LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "     LOWER(g.type) LIKE LOWER(CONCAT('%', :query, '%')) OR "
                    + "     LOWER(g.location) LIKE LOWER(CONCAT('%', :query, '%'))) "
                    + "AND (:userId IS NULL OR g.user_id = :userId) "
                    + "AND (:isPublic IS NULL OR g.is_public = :isPublic) "
                    + "ORDER BY relevance_score DESC",
            nativeQuery = true)
    Page<GardenPlan> searchGardenPlansWithRelevance(
            @Param("name") String name,
            @Param("type") String type,
            @Param("location") String location,
            @Param("query") String query,
            @Param("userId") UUID userId,
            @Param("isPublic") Boolean isPublic,
            Pageable pageable);

    /**
     * Simpler search garden plans by various criteria (fallback method).
     *
     * @param query search term for name, description, type, or location
     * @param userId optional user ID to filter by (can be null)
     * @param isPublic optional flag to filter by public status (can be null)
     * @param pageable pagination and sorting parameters
     * @return page of matching garden plans
     */
    @Transactional(readOnly = true)
    @Query("SELECT g FROM GardenPlan g WHERE "
            + "(:query IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(g.description) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(g.type) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(g.location) LIKE LOWER(CONCAT('%', :query, '%'))) "
            + "AND (:userId IS NULL OR g.userId = :userId) "
            + "AND (:isPublic IS NULL OR g.isPublic = :isPublic)")
    Page<GardenPlan> searchGardenPlans(
            @Param("query") String query,
            @Param("userId") UUID userId,
            @Param("isPublic") Boolean isPublic,
            Pageable pageable);
}
