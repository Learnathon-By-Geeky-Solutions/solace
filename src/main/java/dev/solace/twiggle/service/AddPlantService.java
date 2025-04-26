package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.AddPlantDTO;
import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.mapper.PlantAddMapper;
import dev.solace.twiggle.mapper.PlantMapper;
import dev.solace.twiggle.model.Plant;
import dev.solace.twiggle.model.PlantsLibrary;
import dev.solace.twiggle.repository.PlantRepository;
import dev.solace.twiggle.repository.PlantsLibraryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddPlantService {

    private final PlantRepository plantRepository;
    private final PlantsLibraryRepository plantsLibraryRepository;
    private final PlantMapper plantMapper;
    private final PlantAddMapper plantAddMapper;

    public PlantDTO addFromLibrary(AddPlantDTO addPlantDTO) {

        UUID gardenPlanId = addPlantDTO.getGardenPlanId();
        UUID plantsLibraryId = addPlantDTO.getPlantsLibraryId();

        // Fetch the plant library entry
        PlantsLibrary plantsLibrary = plantsLibraryRepository
                .findById(plantsLibraryId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Plants library entry not found with ID: " + plantsLibraryId));

        // Map plants library to plant entity
        Plant plant = plantAddMapper.toPlantEntity(plantsLibrary);

        // Set garden plan ID and timestamps
        plant.setGardenPlanId(gardenPlanId);
        OffsetDateTime now = OffsetDateTime.now();
        plant.setCreatedAt(now);
        plant.setUpdatedAt(now);

        // Save the plant
        Plant savedPlant = plantRepository.save(plant);

        // Return the DTO
        return plantMapper.toDto(savedPlant);
    }
}
