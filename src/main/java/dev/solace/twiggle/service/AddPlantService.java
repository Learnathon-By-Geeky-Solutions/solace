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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddPlantService {

    private final PlantRepository plantRepository;
    private final PlantsLibraryRepository plantsLibraryRepository;
    private final PlantMapper plantMapper;
    private final PlantAddMapper plantAddMapper;

    // Grid size for plant positioning
    private static final int GRID_SIZE = 10;

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

        // Find an available position for the plant
        setAvailablePosition(plant, gardenPlanId);

        // Save the plant
        Plant savedPlant = plantRepository.save(plant);

        // Return the DTO
        return plantMapper.toDto(savedPlant);
    }

    /**
     * Finds an available position in the garden grid and sets it on the plant.
     *
     * @param plant the plant to position
     * @param gardenPlanId the garden plan ID
     */
    private void setAvailablePosition(Plant plant, UUID gardenPlanId) {
        // Fetch existing plants to check their positions
        List<Plant> existingPlants = plantRepository.findByGardenPlanId(gardenPlanId);

        // Create a set of occupied positions
        Set<String> occupied = new HashSet<>();
        for (Plant existingPlant : existingPlants) {
            if (existingPlant.getPositionX() != null && existingPlant.getPositionY() != null) {
                occupied.add(existingPlant.getPositionX() + "," + existingPlant.getPositionY());
            }
        }

        // Find an available position
        Integer positionX = null;
        Integer positionY = null;

        // Try to find an empty spot in the grid
        boolean found = false;
        for (int y = 0; y < GRID_SIZE && !found; y++) {
            for (int x = 0; x < GRID_SIZE && !found; x++) {
                if (!occupied.contains(x + "," + y)) {
                    positionX = x;
                    positionY = y;
                    found = true;
                    break;
                }
            }
        }

        // Set the position on the plant
        plant.setPositionX(positionX);
        plant.setPositionY(positionY);

        log.debug("Plant positioned at ({}, {})", positionX, positionY);
    }
}
