package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.PlantDiseaseDTO;
import dev.solace.twiggle.mapper.PlantDiseaseMapper;
import dev.solace.twiggle.model.PlantsLibrary;
import dev.solace.twiggle.repository.PlantDiseaseRepository;
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
public class PlantDiseaseService {

    private final PlantDiseaseRepository diseaseRepository;
    private final PlantsLibraryRepository plantsLibraryRepository;
    private final PlantDiseaseMapper plantDiseaseMapper;

    public List<PlantDiseaseDTO> findAll() {
        return diseaseRepository.findAll().stream()
                .map(plantDiseaseMapper::toDto)
                .toList();
    }

    public List<PlantDiseaseDTO> findByPlantLibraryId(UUID plantLibraryId) {
        Optional<PlantsLibrary> optionalPlant = plantsLibraryRepository.findById(plantLibraryId);
        if (optionalPlant.isEmpty()) return List.of();

        Set<String> diseaseNames = optionalPlant.get().getCommonDiseases().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return diseaseRepository.findByCommonNameIgnoreCaseIn(diseaseNames).stream()
                .map(plantDiseaseMapper::toDto)
                .toList();
    }
}
