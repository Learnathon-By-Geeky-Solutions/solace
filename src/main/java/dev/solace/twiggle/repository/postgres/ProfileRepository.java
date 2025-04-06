package dev.solace.twiggle.repository.postgres;

import dev.solace.twiggle.model.postgres.Profile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for the Profile entity.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {}
