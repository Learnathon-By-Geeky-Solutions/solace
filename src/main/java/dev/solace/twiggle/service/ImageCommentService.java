package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.ImageCommentDTO;
import dev.solace.twiggle.mapper.ImageCommentMapper;
import dev.solace.twiggle.model.ImageComment;
import dev.solace.twiggle.repository.ImageCommentRepository;
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
 * Service class for managing image comments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageCommentService {

    private final ImageCommentRepository imageCommentRepository;
    private final ImageCommentMapper imageCommentMapper;

    /**
     * Find all image comments with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of image comment DTOs
     */
    public Page<ImageCommentDTO> findAll(Pageable pageable) {
        return imageCommentRepository.findAll(pageable).map(imageCommentMapper::toDto);
    }

    /**
     * Find all image comments without pagination.
     *
     * @return list of all image comment DTOs
     */
    public List<ImageCommentDTO> findAll() {
        return imageCommentRepository.findAll().stream()
                .map(imageCommentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find image comment by ID.
     *
     * @param id the image comment ID
     * @return optional containing the image comment DTO if found
     */
    public Optional<ImageCommentDTO> findById(UUID id) {
        return imageCommentRepository.findById(id).map(imageCommentMapper::toDto);
    }

    /**
     * Find image comments by image ID with pagination.
     *
     * @param imageId the image ID
     * @param pageable pagination and sorting parameters
     * @return page of image comment DTOs for the image
     */
    public Page<ImageCommentDTO> findByImageId(UUID imageId, Pageable pageable) {
        return imageCommentRepository.findByImageId(imageId, pageable).map(imageCommentMapper::toDto);
    }

    /**
     * Find image comments by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable pagination and sorting parameters
     * @return page of image comment DTOs by the user
     */
    public Page<ImageCommentDTO> findByUserId(UUID userId, Pageable pageable) {
        return imageCommentRepository.findByUserId(userId, pageable).map(imageCommentMapper::toDto);
    }

    /**
     * Count the number of comments for an image.
     *
     * @param imageId the image ID
     * @return the number of comments
     */
    public long countByImageId(UUID imageId) {
        return imageCommentRepository.countByImageId(imageId);
    }

    /**
     * Create a new image comment.
     *
     * @param imageCommentDTO the image comment DTO to create
     * @return the created image comment DTO
     */
    @Transactional
    public ImageCommentDTO create(ImageCommentDTO imageCommentDTO) {
        imageCommentDTO.setCreatedAt(OffsetDateTime.now());

        ImageComment imageComment = imageCommentMapper.toEntity(imageCommentDTO);
        ImageComment savedComment = imageCommentRepository.save(imageComment);

        return imageCommentMapper.toDto(savedComment);
    }

    /**
     * Update an existing image comment.
     *
     * @param id the image comment ID
     * @param imageCommentDTO the updated image comment details
     * @return the updated image comment DTO if found
     */
    @Transactional
    public Optional<ImageCommentDTO> update(UUID id, ImageCommentDTO imageCommentDTO) {
        return imageCommentRepository.findById(id).map(existingComment -> {
            // Update fields from the DTO
            // Note: We typically don't allow changing the imageId or userId after creation
            existingComment.setComment(imageCommentDTO.getComment());

            // Save and convert back to DTO
            return imageCommentMapper.toDto(imageCommentRepository.save(existingComment));
        });
    }

    /**
     * Delete an image comment by ID.
     *
     * @param id the image comment ID
     */
    @Transactional
    public void delete(UUID id) {
        imageCommentRepository.deleteById(id);
    }
}
