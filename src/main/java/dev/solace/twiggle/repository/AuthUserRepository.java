package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.AuthUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    // Custom query methods can be defined here if needed
    // For example, to find a user by email:
    Optional<AuthUser> findByEmail(String email);
}
