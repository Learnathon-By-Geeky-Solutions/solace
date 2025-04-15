package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.Profile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Profile entity.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    /**
     * Find profiles by full name containing the given text (case-insensitive).
     *
     * @param fullName the full name to search for
     * @param pageable pagination and sorting parameters
     * @return page of matching profiles
     */
    Page<Profile> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);

    /**
     * Find profiles by full name containing the given text (case-insensitive) without pagination.
     *
     * @param fullName the full name to search for
     * @return list of matching profiles
     */
    List<Profile> findByFullNameContainingIgnoreCase(String fullName);

    /**
     * Search profiles by various criteria.
     *
     * @param query search term for full name or other fields
     * @param pageable pagination and sorting parameters
     * @return page of matching profiles
     */
    @Query("SELECT p FROM Profile p WHERE "
            + "(:query IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Profile> searchProfiles(@Param("query") String query, Pageable pageable);

    /**
     * Enhanced search for profiles with relevance scoring.
     * The search prioritizes exact matches, then prefix matches, then substring matches.
     * It will return closest matching results when partial entries are provided.
     *
     * @param fullName search term for full name (can be null)
     * @param query general search term for any field (can be null)
     * @param pageable pagination and sorting parameters
     * @return page of matching profiles ordered by relevance
     */
    @Query(
            value = "SELECT p.*, " + "CASE "
                    + "  WHEN LOWER(p.full_name) = LOWER(:fullName) THEN 100 "
                    + "  WHEN LOWER(p.full_name) LIKE LOWER(CONCAT(:fullName, '%')) THEN 80 "
                    + "  WHEN LOWER(p.full_name) LIKE LOWER(CONCAT('%', :fullName, '%')) THEN 60 "
                    + "  ELSE 0 "
                    + "END + "
                    + "CASE "
                    + "  WHEN (:query IS NOT NULL) AND "
                    + "       LOWER(p.full_name) LIKE LOWER(CONCAT('%', :query, '%')) THEN 10 "
                    + "  ELSE 0 "
                    + "END AS relevance_score "
                    + "FROM profiles p "
                    + "WHERE (:fullName IS NULL OR "
                    + "       LOWER(p.full_name) LIKE LOWER(CONCAT('%', :fullName, '%'))) "
                    + "AND (:query IS NULL OR "
                    + "     LOWER(p.full_name) LIKE LOWER(CONCAT('%', :query, '%'))) "
                    + "ORDER BY relevance_score DESC",
            nativeQuery = true)
    Page<Profile> searchProfilesWithRelevance(
            @Param("fullName") String fullName, @Param("query") String query, Pageable pageable);
}
