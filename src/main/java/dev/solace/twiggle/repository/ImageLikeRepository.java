package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.ImageLike;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing ImageLike entities.
 */
@Repository
public interface ImageLikeRepository extends JpaRepository<ImageLike, UUID> {

    /**
     * Find all likes for a specific image with pagination.
     *
     * @param imageId The ID of the image
     * @param pageable Pagination and sorting information
     * @return Page of likes for the image
     */
    Page<ImageLike> findByImageId(UUID imageId, Pageable pageable);

    /**
     * Find all likes for a specific image.
     *
     * @param imageId The ID of the image
     * @return List of likes for the image
     */
    List<ImageLike> findByImageId(UUID imageId);

    /**
     * Find all likes by a specific user with pagination.
     *
     * @param userId The ID of the user
     * @param pageable Pagination and sorting information
     * @return Page of likes by the user
     */
    Page<ImageLike> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all likes by a specific user.
     *
     * @param userId The ID of the user
     * @return List of likes by the user
     */
    List<ImageLike> findByUserId(UUID userId);

    /**
     * Find a like for a specific image by a specific user.
     *
     * @param imageId The ID of the image
     * @param userId The ID of the user
     * @return Optional containing the like if found
     */
    Optional<ImageLike> findByImageIdAndUserId(UUID imageId, UUID userId);

    /**
     * Count the number of likes for a specific image.
     *
     * @param imageId The ID of the image
     * @return The number of likes for the image
     */
    long countByImageId(UUID imageId);

    /**
     * Check if a user has liked a specific image.
     *
     * @param imageId The ID of the image
     * @param userId The ID of the user
     * @return True if the user has liked the image, false otherwise
     */
    boolean existsByImageIdAndUserId(UUID imageId, UUID userId);

    /**
     * Delete a like for a specific image by a specific user.
     *
     * @param imageId The ID of the image
     * @param userId The ID of the user
     */
    void deleteByImageIdAndUserId(UUID imageId, UUID userId);
}
