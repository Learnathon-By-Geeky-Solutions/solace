package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.ImageLikeDTO;
import dev.solace.twiggle.mapper.ImageLikeMapper;
import dev.solace.twiggle.model.ImageLike;
import dev.solace.twiggle.repository.ImageLikeRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing image likes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageLikeService {

    private final ImageLikeRepository imageLikeRepository;
    private final ImageLikeMapper imageLikeMapper;

    /**
     * Find all image likes with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of image like DTOs
     */
    public Page<ImageLikeDTO> findAll(Pageable pageable) {
        return imageLikeRepository.findAll(pageable).map(imageLikeMapper::toDto);
    }

    /**
     * Find all image likes without pagination.
     *
     * @return list of all image like DTOs
     */
    public List<ImageLikeDTO> findAll() {
        return imageLikeRepository.findAll().stream()
                .map(imageLikeMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find image like by ID.
     *
     * @param id the image like ID
     * @return optional containing the image like DTO if found
     */
    public Optional<ImageLikeDTO> findById(UUID id) {
        return imageLikeRepository.findById(id).map(imageLikeMapper::toDto);
    }

    /**
     * Find image likes by image ID with pagination.
     *
     * @param imageId the image ID
     * @param pageable pagination and sorting parameters
     * @return page of image like DTOs for the image
     */
    public Page<ImageLikeDTO> findByImageId(UUID imageId, Pageable pageable) {
        return imageLikeRepository.findByImageId(imageId, pageable).map(imageLikeMapper::toDto);
    }

    /**
     * Find image likes by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination and sorting parameters
     * @return page of image like DTOs by the user
     */
    public Page<ImageLikeDTO> findByUserId(UUID userId, Pageable pageable) {
        return imageLikeRepository.findByUserId(userId, pageable).map(imageLikeMapper::toDto);
    }

    /**
     * Find a like by image ID and user ID.
     *
     * @param imageId the image ID
     * @param userId the user ID
     * @return optional containing the image like DTO if found
     */
    public Optional<ImageLikeDTO> findByImageIdAndUserId(UUID imageId, UUID userId) {
        return imageLikeRepository.findByImageIdAndUserId(imageId, userId).map(imageLikeMapper::toDto);
    }

    /**
     * Check if a user has liked an image.
     *
     * @param imageId the image ID
     * @param userId the user ID
     * @return true if the user has liked the image, false otherwise
     */
    public boolean hasUserLikedImage(UUID imageId, UUID userId) {
        return imageLikeRepository.existsByImageIdAndUserId(imageId, userId);
    }

    /**
     * Count the number of likes for an image.
     *
     * @param imageId the image ID
     * @return the number of likes
     */
    public long countByImageId(UUID imageId) {
        return imageLikeRepository.countByImageId(imageId);
    }

    /**
     * Create a new image like.
     *
     * @param imageLikeDTO the image like DTO to create
     * @return the created image like DTO
     */
    @Transactional
    public ImageLikeDTO create(ImageLikeDTO imageLikeDTO) {
        // Check if the user has already liked the image
        if (imageLikeRepository.existsByImageIdAndUserId(imageLikeDTO.getImageId(), imageLikeDTO.getUserId())) {
            throw new IllegalStateException("User has already liked this image");
        }

        imageLikeDTO.setCreatedAt(OffsetDateTime.now());

        ImageLike imageLike = imageLikeMapper.toEntity(imageLikeDTO);
        ImageLike savedLike = imageLikeRepository.save(imageLike);

        return imageLikeMapper.toDto(savedLike);
    }

    /**
     * Toggle like for an image by a user.
     * If the user has already liked the image, the like is removed.
     * If the user has not liked the image, a new like is created.
     *
     * @param imageId the image ID
     * @param userId the user ID
     * @return true if a like was created, false if a like was removed
     */
    @Transactional
    public boolean toggleLike(UUID imageId, UUID userId) {
        Optional<ImageLike> existingLike = imageLikeRepository.findByImageIdAndUserId(imageId, userId);

        if (existingLike.isPresent()) {
            // Unlike: remove the existing like
            imageLikeRepository.delete(existingLike.get());
            return false;
        } else {
            // Like: create a new like
            ImageLike newLike = new ImageLike();
            newLike.setImageId(imageId);
            newLike.setUserId(userId);
            newLike.setCreatedAt(OffsetDateTime.now());
            imageLikeRepository.save(newLike);
            return true;
        }
    }

    /**
     * Delete an image like by ID.
     *
     * @param id the image like ID
     */
    @Transactional
    public void delete(UUID id) {
        imageLikeRepository.deleteById(id);
    }

    /**
     * Delete a like for an image by a user.
     *
     * @param imageId the image ID
     * @param userId the user ID
     * @return true if a like was deleted, false otherwise
     */
    @Transactional
    public boolean unlikeImage(UUID imageId, UUID userId) {
        if (imageLikeRepository.existsByImageIdAndUserId(imageId, userId)) {
            imageLikeRepository.deleteByImageIdAndUserId(imageId, userId);
            return true;
        }
        return false;
    }
}
