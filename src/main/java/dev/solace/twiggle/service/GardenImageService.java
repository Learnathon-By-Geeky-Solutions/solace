package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.GardenImageDTO;
import dev.solace.twiggle.mapper.GardenImageMapper;
import dev.solace.twiggle.model.GardenImage;
import dev.solace.twiggle.repository.GardenImageRepository;
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
 * Service class for managing garden images.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GardenImageService {

    private final GardenImageRepository gardenImageRepository;
    private final GardenImageMapper gardenImageMapper;

    /**
     * Find all garden images with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of garden image DTOs
     */
    public Page<GardenImageDTO> findAll(Pageable pageable) {
        return gardenImageRepository.findAll(pageable).map(gardenImageMapper::toDto);
    }

    /**
     * Find all garden images without pagination.
     *
     * @return list of all garden image DTOs
     */
    public List<GardenImageDTO> findAll() {
        return gardenImageRepository.findAll().stream()
                .map(gardenImageMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find garden image by ID.
     *
     * @param id the garden image ID
     * @return optional containing the garden image DTO if found
     */
    public Optional<GardenImageDTO> findById(UUID id) {
        return gardenImageRepository.findById(id).map(gardenImageMapper::toDto);
    }

    /**
     * Find garden images by garden plan ID with pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @param pageable pagination and sorting parameters
     * @return page of garden image DTOs for the garden plan
     */
    public Page<GardenImageDTO> findByGardenPlanId(UUID gardenPlanId, Pageable pageable) {
        return gardenImageRepository.findByGardenPlanId(gardenPlanId, pageable).map(gardenImageMapper::toDto);
    }

    /**
     * Find garden images by garden plan ID without pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @return list of garden image DTOs for the garden plan
     */
    public List<GardenImageDTO> findByGardenPlanId(UUID gardenPlanId) {
        return gardenImageRepository.findByGardenPlanId(gardenPlanId).stream()
                .map(gardenImageMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find garden images by garden plan ID and title with pagination.
     *
     * @param gardenPlanId the garden plan ID
     * @param title the title to search for (partial, case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return page of garden image DTOs for the garden plan with matching title
     */
    public Page<GardenImageDTO> findByGardenPlanIdAndTitle(UUID gardenPlanId, String title, Pageable pageable) {
        return gardenImageRepository
                .findByGardenPlanIdAndTitleContainingIgnoreCase(gardenPlanId, title, pageable)
                .map(gardenImageMapper::toDto);
    }

    /**
     * Search garden images by title with pagination.
     *
     * @param title the title to search for (partial, case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return page of garden image DTOs with matching title
     */
    public Page<GardenImageDTO> searchByTitle(String title, Pageable pageable) {
        return gardenImageRepository
                .findByTitleContainingIgnoreCase(title, pageable)
                .map(gardenImageMapper::toDto);
    }

    /**
     * Create a new garden image.
     *
     * @param gardenImageDTO the garden image DTO to create
     * @return the created garden image DTO
     */
    @Transactional
    public GardenImageDTO create(GardenImageDTO gardenImageDTO) {
        gardenImageDTO.setCreatedAt(OffsetDateTime.now());

        GardenImage gardenImage = gardenImageMapper.toEntity(gardenImageDTO);
        GardenImage savedImage = gardenImageRepository.save(gardenImage);

        return gardenImageMapper.toDto(savedImage);
    }

    /**
     * Update an existing garden image.
     *
     * @param id the garden image ID
     * @param gardenImageDTO the updated garden image details
     * @return the updated garden image DTO if found
     */
    @Transactional
    public Optional<GardenImageDTO> update(UUID id, GardenImageDTO gardenImageDTO) {
        return gardenImageRepository.findById(id).map(existingImage -> {
            // Update fields from the DTO
            existingImage.setGardenPlanId(gardenImageDTO.getGardenPlanId());
            existingImage.setImageUrl(gardenImageDTO.getImageUrl());
            existingImage.setTitle(gardenImageDTO.getTitle());

            // Save and convert back to DTO
            return gardenImageMapper.toDto(gardenImageRepository.save(existingImage));
        });
    }

    /**
     * Delete a garden image by ID.
     *
     * @param id the garden image ID
     */
    @Transactional
    public void delete(UUID id) {
        gardenImageRepository.deleteById(id);
    }
}
