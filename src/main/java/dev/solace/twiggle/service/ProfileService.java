package dev.solace.twiggle.service;

import dev.solace.twiggle.model.postgres.Profile;
import dev.solace.twiggle.repository.postgres.ProfileRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Find all profiles.
     *
     * @return list of all profiles
     */
    public List<Profile> findAll() {
        return profileRepository.findAll();
    }

    /**
     * Find profile by ID.
     *
     * @param id the profile ID
     * @return optional containing the profile if found
     */
    public Optional<Profile> findById(UUID id) {
        return profileRepository.findById(id);
    }

    /**
     * Create a new profile.
     *
     * @param profile the profile to create
     * @return the created profile
     */
    @Transactional
    public Profile create(Profile profile) {
        OffsetDateTime now = OffsetDateTime.now();
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        return profileRepository.save(profile);
    }

    /**
     * Update an existing profile.
     *
     * @param id the profile ID
     * @param profile the updated profile details
     * @return the updated profile
     */
    @Transactional
    public Optional<Profile> update(UUID id, Profile profile) {
        return profileRepository.findById(id).map(existingProfile -> {
            existingProfile.setFullName(profile.getFullName());
            existingProfile.setAvatarUrl(profile.getAvatarUrl());
            existingProfile.setUpdatedAt(OffsetDateTime.now());
            return profileRepository.save(existingProfile);
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
