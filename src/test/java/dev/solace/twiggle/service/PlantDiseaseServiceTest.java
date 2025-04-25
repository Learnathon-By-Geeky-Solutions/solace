package dev.solace.twiggle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.PlantDiseaseDTO;
import dev.solace.twiggle.mapper.PlantDiseaseMapper;
import dev.solace.twiggle.model.PlantDisease;
import dev.solace.twiggle.model.PlantsLibrary;
import dev.solace.twiggle.repository.PlantDiseaseRepository;
import dev.solace.twiggle.repository.PlantsLibraryRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlantDiseaseServiceTest {

    @Mock
    private PlantDiseaseRepository diseaseRepository;

    @Mock
    private PlantsLibraryRepository plantsLibraryRepository;

    @Mock
    private PlantDiseaseMapper plantDiseaseMapper;

    @InjectMocks
    private PlantDiseaseService plantDiseaseService;

    private PlantDisease disease1;
    private PlantDisease disease2;
    private PlantDiseaseDTO diseaseDTO1;
    private PlantDiseaseDTO diseaseDTO2;
    private PlantsLibrary plantLibrary;
    private UUID plantLibraryId;
    private List<String> commonDiseases;
    private List<String> lowerCaseDiseases;

    @BeforeEach
    void setUp() {
        plantLibraryId = UUID.randomUUID();

        disease1 = new PlantDisease();
        disease1.setId(1L);
        disease1.setCommonName("Leaf Blight");

        disease2 = new PlantDisease();
        disease2.setId(2L);
        disease2.setCommonName("Root Rot");

        diseaseDTO1 = new PlantDiseaseDTO();
        diseaseDTO1.setId(1L);
        diseaseDTO1.setCommonName(disease1.getCommonName());

        diseaseDTO2 = new PlantDiseaseDTO();
        diseaseDTO2.setId(2L);
        diseaseDTO2.setCommonName(disease2.getCommonName());

        commonDiseases = Arrays.asList("Leaf Blight", "Root Rot");
        lowerCaseDiseases = Arrays.asList("leaf blight", "root rot");

        plantLibrary = new PlantsLibrary();
        plantLibrary.setId(plantLibraryId);
        plantLibrary.setCommonDiseases(commonDiseases);
    }

    @Test
    void findAll_ShouldReturnAllDiseases() {
        // Arrange
        List<PlantDisease> diseases = Arrays.asList(disease1, disease2);
        doReturn(diseases).when(diseaseRepository).findAll();
        doReturn(diseaseDTO1).when(plantDiseaseMapper).toDto(disease1);
        doReturn(diseaseDTO2).when(plantDiseaseMapper).toDto(disease2);

        // Act
        List<PlantDiseaseDTO> result = plantDiseaseService.findAll();

        // Assert
        assertThat(result).hasSize(2).containsExactly(diseaseDTO1, diseaseDTO2);

        verify(diseaseRepository).findAll();
        verify(plantDiseaseMapper, times(2)).toDto(any(PlantDisease.class));
    }

    @Test
    void findByPlantLibraryId_WhenPlantExists_ShouldReturnMatchingDiseases() {
        // Arrange
        doReturn(Optional.of(plantLibrary)).when(plantsLibraryRepository).findById(plantLibraryId);
        doReturn(Arrays.asList(disease1, disease2)).when(diseaseRepository).findByCommonNameIgnoreCaseIn(any());
        doReturn(diseaseDTO1).when(plantDiseaseMapper).toDto(disease1);
        doReturn(diseaseDTO2).when(plantDiseaseMapper).toDto(disease2);

        // Act
        List<PlantDiseaseDTO> result = plantDiseaseService.findByPlantLibraryId(plantLibraryId);

        // Assert
        assertThat(result).hasSize(2).containsExactly(diseaseDTO1, diseaseDTO2);

        verify(plantsLibraryRepository).findById(plantLibraryId);
        verify(diseaseRepository).findByCommonNameIgnoreCaseIn(any());
        verify(plantDiseaseMapper, times(2)).toDto(any(PlantDisease.class));
    }

    @Test
    void findByPlantLibraryId_WhenPlantDoesNotExist_ShouldReturnEmptyList() {
        // Arrange
        doReturn(Optional.empty()).when(plantsLibraryRepository).findById(plantLibraryId);

        // Act
        List<PlantDiseaseDTO> result = plantDiseaseService.findByPlantLibraryId(plantLibraryId);

        // Assert
        assertThat(result).isEmpty();

        verify(plantsLibraryRepository).findById(plantLibraryId);
        verify(diseaseRepository, never()).findByCommonNameIgnoreCaseIn(any());
        verify(plantDiseaseMapper, never()).toDto(any());
    }
}
