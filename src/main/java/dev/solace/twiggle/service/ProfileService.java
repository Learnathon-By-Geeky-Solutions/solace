package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.ProfileDTO;
import dev.solace.twiggle.mapper.ProfileMapper;
import dev.solace.twiggle.model.Profile;
import dev.solace.twiggle.repository.ProfileRepository;
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
 * Service class for managing user profiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    /**
     * Find all profiles with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return page of profile DTOs
     */
    public Page<ProfileDTO> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable).map(profileMapper::toDto);
    }

    /**
     * Find all profiles without pagination.
     *
     * @return list of all profile DTOs
     */
    public List<ProfileDTO> findAll() {
        return profileRepository.findAll().stream().map(profileMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Find profile by ID.
     *
     * @param id the profile ID
     * @return optional containing the profile DTO if found
     */
    public Optional<ProfileDTO> findById(UUID id) {
        return profileRepository.findById(id).map(profileMapper::toDto);
    }

    /**
     * Find profiles by name with pagination and sorting.
     *
     * @param fullName the name to search for
     * @param pageable pagination and sorting parameters
     * @return page of profile DTOs
     */
    public Page<ProfileDTO> findByFullName(String fullName, Pageable pageable) {
        return profileRepository
                .findByFullNameContainingIgnoreCase(fullName, pageable)
                .map(profileMapper::toDto);
    }

    /**
     * Find profiles by name without pagination.
     *
     * @param fullName the name to search for
     * @return list of profile DTOs
     */
    public List<ProfileDTO> findByFullName(String fullName) {
        return profileRepository.findByFullNameContainingIgnoreCase(fullName).stream()
                .map(profileMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search profiles by query.
     *
     * @param query search query for name or other fields (optional)
     * @param pageable pagination and sorting parameters
     * @return page of matching profile DTOs
     */
    public Page<ProfileDTO> searchProfiles(String query, Pageable pageable) {
        return profileRepository.searchProfiles(query, pageable).map(profileMapper::toDto);
    }

    /**
     * Enhanced search for profiles with relevance scoring.
     *
     * @param fullName search term for full name (optional)
     * @param query general search term for any field (optional)
     * @param pageable pagination and sorting parameters
     * @return page of matching profile DTOs ordered by relevance
     */
    public Page<ProfileDTO> searchProfilesWithRelevance(String fullName, String query, Pageable pageable) {
        try {
            return profileRepository
                    .searchProfilesWithRelevance(fullName, query, pageable)
                    .map(profileMapper::toDto);
        } catch (Exception e) {
            // If the enhanced search fails, fall back to the simpler search method
            log.warn("Enhanced profile search failed, falling back to simple search: {}", e.getMessage());
            return profileRepository.searchProfiles(query, pageable).map(profileMapper::toDto);
        }
    }

    /**
     * Create a new profile.
     *
     * @param profileDTO the profile DTO to create
     * @return the created profile DTO
     */
    @Transactional
    public ProfileDTO create(ProfileDTO profileDTO) {
        OffsetDateTime now = OffsetDateTime.now();
        profileDTO.setCreatedAt(now);
        profileDTO.setUpdatedAt(now);

        Profile profile = profileMapper.toEntity(profileDTO);
        Profile savedProfile = profileRepository.save(profile);

        return profileMapper.toDto(savedProfile);
    }

    /**
     * Update an existing profile.
     *
     * @param id the profile ID
     * @param profileDTO the updated profile details
     * @return the updated profile DTO if found
     */
    @Transactional
    public Optional<ProfileDTO> update(UUID id, ProfileDTO profileDTO) {
        return profileRepository.findById(id).map(existingProfile -> {
            // Update fields from the DTO
            existingProfile.setFullName(profileDTO.getFullName());
            existingProfile.setAvatarUrl(profileDTO.getAvatarUrl());
            existingProfile.setUpdatedAt(OffsetDateTime.now());

            // Save and convert back to DTO
            return profileMapper.toDto(profileRepository.save(existingProfile));
        });
    }

    /**
     * Delete a profile by ID.
     *
     * @param id the profile ID
     */
    @Transactional
    public void delete(UUID id) {
        profileRepository.deleteById(id);
    }
}
