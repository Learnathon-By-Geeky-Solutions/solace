package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.Pest;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PestRepository extends JpaRepository<Pest, Long> {
    List<Pest> findByCommonNameIgnoreCaseIn(Collection<String> names);
}
