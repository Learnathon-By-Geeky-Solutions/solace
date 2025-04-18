package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.solace.twiggle.dto.plant.PlantCreateRequest;
import dev.solace.twiggle.dto.plant.PlantResponse;
import dev.solace.twiggle.dto.plant.PlantUpdateRequest;
import dev.solace.twiggle.exception.ResourceNotFoundException;
import dev.solace.twiggle.mapper.PlantMapper;
import dev.solace.twiggle.model.Plant;
import dev.solace.twiggle.repository.PlantRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PlantServiceTest {

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private PlantMapper plantMapper;

    @InjectMocks
    private PlantService plantService;

    private Plant plant1;
    private Plant plant2;
    private PlantResponse plantResponse1;
    private PlantResponse plantResponse2;
    private PlantCreateRequest createRequest;
    private PlantUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Set up test plants
        plant1 = new Plant();
        plant1.setId(1L);
        plant1.setCommonName("Tomato");
        plant1.setScientificName("Solanum lycopersicum");
        plant1.setSunlight("Full sun");
        plant1.setWateringFrequency("Regular");
        plant1.setDescription("Common garden vegetable");

        plant2 = new Plant();
        plant2.setId(2L);
        plant2.setCommonName("Basil");
        plant2.setScientificName("Ocimum basilicum");
        plant2.setSunlight("Partial to full sun");
        plant2.setWateringFrequency("Moderate");
        plant2.setDescription("Aromatic herb");

        // Set up plant responses
        plantResponse1 = new PlantResponse();
        plantResponse1.setId(1L);
        plantResponse1.setCommonName("Tomato");
        plantResponse1.setScientificName("Solanum lycopersicum");
        plantResponse1.setSunlight("Full sun");
        plantResponse1.setWateringFrequency("Regular");
        plantResponse1.setDescription("Common garden vegetable");

        plantResponse2 = new PlantResponse();
        plantResponse2.setId(2L);
        plantResponse2.setCommonName("Basil");
        plantResponse2.setScientificName("Ocimum basilicum");
        plantResponse2.setSunlight("Partial to full sun");
        plantResponse2.setWateringFrequency("Moderate");
        plantResponse2.setDescription("Aromatic herb");

        // Set up create request
        createRequest = new PlantCreateRequest();
        createRequest.setCommonName("Cucumber");
        createRequest.setScientificName("Cucumis sativus");
        createRequest.setSunlight("Full sun");
        createRequest.setWateringFrequency("Regular");
        createRequest.setDescription("Refreshing vegetable");

        // Set up update request
        updateRequest = new PlantUpdateRequest();
        updateRequest.setCommonName("Roma Tomato");
        updateRequest.setScientificName("Solanum lycopersicum var. roma");
        updateRequest.setSunlight("Full sun");
        updateRequest.setWateringFrequency("Regular");
        updateRequest.setDescription("Tomato variety good for sauces");
    }

    @Test
    void getAllPlants_ShouldReturnPageOfPlants() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plant> plantPage = new PageImpl<>(List.of(plant1, plant2));

        when(plantRepository.findAll(pageable)).thenReturn(plantPage);
        when(plantMapper.toPlantResponse(plant1)).thenReturn(plantResponse1);
        when(plantMapper.toPlantResponse(plant2)).thenReturn(plantResponse2);

        // Act
        Page<PlantResponse> result = plantService.getAllPlants(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(plantResponse1, result.getContent().get(0));
        assertEquals(plantResponse2, result.getContent().get(1));
        verify(plantRepository, times(1)).findAll(pageable);
    }

    @Test
    void getPlantById_WhenPlantExists_ShouldReturnPlant() {
        // Arrange
        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant1));
        when(plantMapper.toPlantResponse(plant1)).thenReturn(plantResponse1);

        // Act
        PlantResponse result = plantService.getPlantById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(plantResponse1, result);
        verify(plantRepository, times(1)).findById(1L);
    }

    @Test
    void getPlantById_WhenPlantDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(plantRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> plantService.getPlantById(99L));
        verify(plantRepository, times(1)).findById(99L);
    }

    @Test
    void createPlant_ShouldReturnCreatedPlant() {
        // Arrange
        Plant newPlant = new Plant();
        newPlant.setCommonName("Cucumber");
        newPlant.setScientificName("Cucumis sativus");

        Plant savedPlant = new Plant();
        savedPlant.setId(3L);
        savedPlant.setCommonName("Cucumber");
        savedPlant.setScientificName("Cucumis sativus");

        PlantResponse newPlantResponse = new PlantResponse();
        newPlantResponse.setId(3L);
        newPlantResponse.setCommonName("Cucumber");
        newPlantResponse.setScientificName("Cucumis sativus");

        when(plantMapper.toPlant(createRequest)).thenReturn(newPlant);
        when(plantRepository.save(newPlant)).thenReturn(savedPlant);
        when(plantMapper.toPlantResponse(savedPlant)).thenReturn(newPlantResponse);

        // Act
        PlantResponse result = plantService.createPlant(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(newPlantResponse, result);
        verify(plantRepository, times(1)).save(newPlant);
    }

    @Test
    void updatePlant_WhenPlantExists_ShouldReturnUpdatedPlant() {
        // Arrange
        Plant updatedPlant = new Plant();
        updatedPlant.setId(1L);
        updatedPlant.setCommonName("Roma Tomato");

        PlantResponse updatedResponse = new PlantResponse();
        updatedResponse.setId(1L);
        updatedResponse.setCommonName("Roma Tomato");

        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant1));
        when(plantMapper.updatePlantFromRequest(updateRequest, plant1)).thenReturn(updatedPlant);
        when(plantRepository.save(updatedPlant)).thenReturn(updatedPlant);
        when(plantMapper.toPlantResponse(updatedPlant)).thenReturn(updatedResponse);

        // Act
        PlantResponse result = plantService.updatePlant(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updatedResponse, result);
        verify(plantRepository, times(1)).findById(1L);
        verify(plantRepository, times(1)).save(updatedPlant);
    }

    @Test
    void updatePlant_WhenPlantDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(plantRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> plantService.updatePlant(99L, updateRequest));
        verify(plantRepository, times(1)).findById(99L);
    }

    @Test
    void deletePlant_WhenPlantExists_ShouldDeletePlant() {
        // Arrange
        when(plantRepository.existsById(1L)).thenReturn(true);

        // Act
        plantService.deletePlant(1L);

        // Assert
        verify(plantRepository, times(1)).existsById(1L);
        verify(plantRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletePlant_WhenPlantDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(plantRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> plantService.deletePlant(99L));
        verify(plantRepository, times(1)).existsById(99L);
    }

    @Test
    void searchPlantsByName_ShouldReturnMatchingPlants() {
        // Arrange
        String searchTerm = "Tom";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plant> plantPage = new PageImpl<>(List.of(plant1));

        when(plantRepository.findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(
                        eq(searchTerm), eq(searchTerm), eq(pageable)))
                .thenReturn(plantPage);
        when(plantMapper.toPlantResponse(plant1)).thenReturn(plantResponse1);

        // Act
        Page<PlantResponse> result = plantService.searchPlantsByName(searchTerm, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(plantResponse1, result.getContent().get(0));
        verify(plantRepository, times(1))
                .findByCommonNameContainingIgnoreCaseOrScientificNameContainingIgnoreCase(
                        eq(searchTerm), eq(searchTerm), eq(pageable));
    }
}
