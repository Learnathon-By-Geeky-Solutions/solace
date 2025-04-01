package dev.solace.twiggle.repository.postgres;

import dev.solace.twiggle.model.postgres.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for the Profile entity.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
} 