package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
