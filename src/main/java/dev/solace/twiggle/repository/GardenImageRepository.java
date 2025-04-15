package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.GardenImage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing GardenImage entities.
 */
@Repository
public interface GardenImageRepository extends JpaRepository<GardenImage, UUID> {

    /**
     * Find all images for a specific garden plan with pagination.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param pageable Pagination and sorting information
     * @return Page of images for the garden plan
     */
    Page<GardenImage> findByGardenPlanId(UUID gardenPlanId, Pageable pageable);

    /**
     * Find all images for a specific garden plan.
     *
     * @param gardenPlanId The ID of the garden plan
     * @return List of images for the garden plan
     */
    List<GardenImage> findByGardenPlanId(UUID gardenPlanId);

    /**
     * Find all images for a specific garden plan with a specific title (case-insensitive, partial match) with pagination.
     *
     * @param gardenPlanId The ID of the garden plan
     * @param title The title to search for
     * @param pageable Pagination and sorting information
     * @return Page of images for the garden plan with the specified title
     */
    Page<GardenImage> findByGardenPlanIdAndTitleContainingIgnoreCase(
            UUID gardenPlanId, String title, Pageable pageable);

    /**
     * Find all images for a specific garden plan with a specific title (case-insensitive, partial match).
     *
     * @param gardenPlanId The ID of the garden plan
     * @param title The title to search for
     * @return List of images for the garden plan with the specified title
     */
    List<GardenImage> findByGardenPlanIdAndTitleContainingIgnoreCase(UUID gardenPlanId, String title);

    /**
     * Find all images with a specific title (case-insensitive, partial match) with pagination.
     *
     * @param title The title to search for
     * @param pageable Pagination and sorting information
     * @return Page of images with the specified title
     */
    Page<GardenImage> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
