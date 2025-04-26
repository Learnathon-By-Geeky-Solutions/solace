package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.PestDTO;
import dev.solace.twiggle.mapper.PestMapper;
import dev.solace.twiggle.model.PlantsLibrary;
import dev.solace.twiggle.repository.PestRepository;
import dev.solace.twiggle.repository.PlantsLibraryRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PestService {

    private final PestRepository pestRepository;
    private final PlantsLibraryRepository plantsLibraryRepository;
    private final PestMapper pestMapper;

    public List<PestDTO> findAll() {
        return pestRepository.findAll().stream().map(pestMapper::toDto).toList();
    }

    public List<PestDTO> findByPlantLibraryId(UUID plantLibraryId) {
        Optional<PlantsLibrary> optionalPlant = plantsLibraryRepository.findById(plantLibraryId);
        if (optionalPlant.isEmpty()) {
            return List.of();
        }

        Set<String> pestNames = optionalPlant.get().getCommonPests().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return pestRepository.findByCommonNameIgnoreCaseIn(pestNames).stream()
                .map(pestMapper::toDto)
                .toList();
    }
}
