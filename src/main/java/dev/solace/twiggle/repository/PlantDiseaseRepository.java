package dev.solace.twiggle.repository;

import dev.solace.twiggle.model.PlantDisease;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantDiseaseRepository extends JpaRepository<PlantDisease, Long> {
    List<PlantDisease> findByCommonNameIgnoreCaseIn(Collection<String> names);
}
