package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddPlantServiceTest {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private PlantsLibraryRepository plantsLibraryRepository;

    @Mock
    private PlantMapper plantMapper;

    @Mock
    private PlantAddMapper plantAddMapper;

    @InjectMocks
    private AddPlantService addPlantService;

    private UUID gardenPlanId;
    private UUID plantsLibraryId;
    private AddPlantDTO addPlantDTO;
    private PlantsLibrary plantsLibrary;
    private Plant plant;
    private Plant savedPlant;
    private PlantDTO plantDTO;

    @BeforeEach
    void setUp() {
        gardenPlanId = UUID.randomUUID();
        plantsLibraryId = UUID.randomUUID();

        addPlantDTO = AddPlantDTO.builder()
                .gardenPlanId(gardenPlanId)
                .plantsLibraryId(plantsLibraryId)
                .build();

        plantsLibrary = PlantsLibrary.builder()
                .id(plantsLibraryId)
                .commonName("Basil")
                .plantType("Herb")
                .shortDescription("A fragrant herb used in cooking")
                .wateringFrequency("Daily")
                .sunlightRequirement("Full Sun")
                .imageUrl("https://example.com/basil.jpg")
                .build();

        plant = new Plant();
        plant.setName("Basil");
        plant.setType("Herb");
        plant.setDescription("A fragrant herb used in cooking");
        plant.setWateringFrequency("Daily");
        plant.setSunlightRequirements("Full Sun");
        plant.setImageUrl("https://example.com/basil.jpg");

        savedPlant = new Plant();
        savedPlant.setId(UUID.randomUUID());
        savedPlant.setGardenPlanId(gardenPlanId);
        savedPlant.setName("Basil");
        savedPlant.setType("Herb");
        savedPlant.setDescription("A fragrant herb used in cooking");
        savedPlant.setWateringFrequency("Daily");
        savedPlant.setSunlightRequirements("Full Sun");
        savedPlant.setImageUrl("https://example.com/basil.jpg");
        savedPlant.setPositionX(0);
        savedPlant.setPositionY(0);
        savedPlant.setCreatedAt(OffsetDateTime.now());
        savedPlant.setUpdatedAt(OffsetDateTime.now());

        plantDTO = PlantDTO.builder()
                .gardenPlanId(gardenPlanId)
                .name("Basil")
                .type("Herb")
                .description("A fragrant herb used in cooking")
                .wateringFrequency("Daily")
                .sunlightRequirements("Full Sun")
                .imageUrl("https://example.com/basil.jpg")
                .positionX(0)
                .positionY(0)
                .createdAt(savedPlant.getCreatedAt())
                .updatedAt(savedPlant.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("addFromLibrary should successfully add a plant from library")
    void addFromLibrary_ShouldAddPlantFromLibrary() {
        // Arrange
        when(plantsLibraryRepository.findById(plantsLibraryId)).thenReturn(Optional.of(plantsLibrary));
        when(plantAddMapper.toPlantEntity(plantsLibrary)).thenReturn(plant);
        when(plantRepository.findByGardenPlanId(gardenPlanId)).thenReturn(new ArrayList<>());
        when(plantRepository.save(any(Plant.class))).thenReturn(savedPlant);
        when(plantMapper.toDto(savedPlant)).thenReturn(plantDTO);

        // Act
        PlantDTO result = addPlantService.addFromLibrary(addPlantDTO);

        // Assert
        assertNotNull(result);
        assertEquals(gardenPlanId, result.getGardenPlanId());
        assertEquals("Basil", result.getName());
        assertEquals("Herb", result.getType());
        assertEquals("A fragrant herb used in cooking", result.getDescription());
        assertEquals("Daily", result.getWateringFrequency());
        assertEquals("Full Sun", result.getSunlightRequirements());
        assertEquals("https://example.com/basil.jpg", result.getImageUrl());
        assertEquals(0, result.getPositionX());
        assertEquals(0, result.getPositionY());

        // Verify that position was set before saving
        ArgumentCaptor<Plant> plantCaptor = forClass(Plant.class);
        verify(plantRepository).save(plantCaptor.capture());
        Plant capturedPlant = plantCaptor.getValue();
        assertEquals(0, capturedPlant.getPositionX());
        assertEquals(0, capturedPlant.getPositionY());
    }

    @Test
    @DisplayName("addFromLibrary should position plants correctly when some positions are occupied")
    void addFromLibrary_ShouldPositionPlants_WhenSomePositionsAreOccupied() {
        // Arrange
        when(plantsLibraryRepository.findById(plantsLibraryId)).thenReturn(Optional.of(plantsLibrary));
        when(plantAddMapper.toPlantEntity(plantsLibrary)).thenReturn(plant);

        // Create a list of existing plants with positions (0,0) and (0,1) occupied
        List<Plant> existingPlants = new ArrayList<>();
        Plant existingPlant1 = new Plant();
        existingPlant1.setPositionX(0);
        existingPlant1.setPositionY(0);
        Plant existingPlant2 = new Plant();
        existingPlant2.setPositionX(0);
        existingPlant2.setPositionY(1);
        existingPlants.add(existingPlant1);
        existingPlants.add(existingPlant2);

        when(plantRepository.findByGardenPlanId(gardenPlanId)).thenReturn(existingPlants);

        // Update saved plant to have position (1,0) which should be first available
        // since the search goes by y first then x (e.g. (0,0), (0,1), (0,2)... then (1,0), (1,1), etc.)
        savedPlant.setPositionX(1);
        savedPlant.setPositionY(0);
        when(plantRepository.save(any(Plant.class))).thenReturn(savedPlant);

        // Update DTO to match the new position
        plantDTO.setPositionX(1);
        plantDTO.setPositionY(0);
        when(plantMapper.toDto(savedPlant)).thenReturn(plantDTO);

        // Act
        PlantDTO result = addPlantService.addFromLibrary(addPlantDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPositionX());
        assertEquals(0, result.getPositionY());

        // Verify that position was set to (1,0) before saving
        ArgumentCaptor<Plant> plantCaptor = forClass(Plant.class);
        verify(plantRepository).save(plantCaptor.capture());
        Plant capturedPlant = plantCaptor.getValue();
        assertEquals(1, capturedPlant.getPositionX());
        assertEquals(0, capturedPlant.getPositionY());
    }

    @Test
    @DisplayName("addFromLibrary should throw EntityNotFoundException when plants library entry not found")
    void addFromLibrary_ShouldThrowEntityNotFoundException_WhenPlantsLibraryNotFound() {
        // Arrange
        when(plantsLibraryRepository.findById(plantsLibraryId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> addPlantService.addFromLibrary(addPlantDTO));
        assertEquals("Plants library entry not found with ID: " + plantsLibraryId, exception.getMessage());
    }
}
