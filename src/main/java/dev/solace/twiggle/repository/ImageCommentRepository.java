package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.ImageComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing ImageComment entities.
 */
@Repository
public interface ImageCommentRepository extends JpaRepository<ImageComment, UUID> {

    /**
     * Find all comments for a specific image with pagination.
     *
     * @param imageId The ID of the image
     * @param pageable Pagination and sorting information
     * @return Page of comments for the image
     */
    Page<ImageComment> findByImageId(UUID imageId, Pageable pageable);

    /**
     * Find all comments for a specific image.
     *
     * @param imageId The ID of the image
     * @return List of comments for the image
     */
    List<ImageComment> findByImageId(UUID imageId);

    /**
     * Find all comments by a specific user with pagination.
     *
     * @param userId The ID of the user
     * @param pageable Pagination and sorting information
     * @return Page of comments by the user
     */
    Page<ImageComment> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find all comments by a specific user.
     *
     * @param userId The ID of the user
     * @return List of comments by the user
     */
    List<ImageComment> findByUserId(UUID userId);

    /**
     * Find all comments for a specific image by a specific user with pagination.
     *
     * @param imageId The ID of the image
     * @param userId The ID of the user
     * @param pageable Pagination and sorting information
     * @return Page of comments for the image by the user
     */
    Page<ImageComment> findByImageIdAndUserId(UUID imageId, UUID userId, Pageable pageable);

    /**
     * Find all comments for a specific image by a specific user.
     *
     * @param imageId The ID of the image
     * @param userId The ID of the user
     * @return List of comments for the image by the user
     */
    List<ImageComment> findByImageIdAndUserId(UUID imageId, UUID userId);

    /**
     * Count the number of comments for a specific image.
     *
     * @param imageId The ID of the image
     * @return The number of comments for the image
     */
    long countByImageId(UUID imageId);
}
