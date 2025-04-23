package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.mapper.PlantMapper;
import dev.solace.twiggle.model.Plant;
import dev.solace.twiggle.repository.PlantRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private PlantDTO plantDTO1;
    private PlantDTO plantDTO2;
    private UUID plant1Uuid;
    private UUID plant2Uuid;
    private UUID gardenPlanUuid;

    @BeforeEach
    void setUp() {
        plant1Uuid = UUID.randomUUID();
        plant2Uuid = UUID.randomUUID();
        gardenPlanUuid = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        plant1 = new Plant();
        plant1.setId(plant1Uuid);
        plant1.setGardenPlanId(gardenPlanUuid);
        plant1.setName("Tomato");
        plant1.setType("Vegetable");
        plant1.setDescription("Desc1");
        plant1.setWateringFrequency("Freq1");
        plant1.setSunlightRequirements("Sun1");
        plant1.setPositionX(1);
        plant1.setPositionY(1);
        plant1.setImageUrl("url1");
        plant1.setCreatedAt(now.minusDays(1));
        plant1.setUpdatedAt(now);

        plant2 = new Plant();
        plant2.setId(plant2Uuid);
        plant2.setGardenPlanId(gardenPlanUuid);
        plant2.setName("Basil");
        plant2.setType("Herb");
        plant2.setDescription("Desc2");
        plant2.setWateringFrequency("Freq2");
        plant2.setSunlightRequirements("Sun2");
        plant2.setPositionX(2);
        plant2.setPositionY(2);
        plant2.setImageUrl("url2");
        plant2.setCreatedAt(now.minusHours(5));
        plant2.setUpdatedAt(now.minusHours(1));

        plantDTO1 = PlantDTO.builder()
                .gardenPlanId(plant1.getGardenPlanId())
                .name(plant1.getName())
                .type(plant1.getType())
                .description(plant1.getDescription())
                .wateringFrequency(plant1.getWateringFrequency())
                .sunlightRequirements(plant1.getSunlightRequirements())
                .positionX(plant1.getPositionX())
                .positionY(plant1.getPositionY())
                .imageUrl(plant1.getImageUrl())
                .createdAt(plant1.getCreatedAt())
                .updatedAt(plant1.getUpdatedAt())
                .build();

        plantDTO2 = PlantDTO.builder()
                .gardenPlanId(plant2.getGardenPlanId())
                .name(plant2.getName())
                .type(plant2.getType())
                .description(plant2.getDescription())
                .wateringFrequency(plant2.getWateringFrequency())
                .sunlightRequirements(plant2.getSunlightRequirements())
                .positionX(plant2.getPositionX())
                .positionY(plant2.getPositionY())
                .imageUrl(plant2.getImageUrl())
                .createdAt(plant2.getCreatedAt())
                .updatedAt(plant2.getUpdatedAt())
                .build();
    }

    @Test
    void findAll_ShouldReturnPageOfPlantDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Plant> plantPage = new PageImpl<>(List.of(plant1, plant2), pageable, 2);

        when(plantRepository.findAll(pageable)).thenReturn(plantPage);
        when(plantMapper.toDto(plant1)).thenReturn(plantDTO1);
        when(plantMapper.toDto(plant2)).thenReturn(plantDTO2);

        Page<PlantDTO> result = plantService.findAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(plantDTO1, result.getContent().get(0));
        assertEquals(plantDTO2, result.getContent().get(1));
        verify(plantRepository).findAll(pageable);
        verify(plantMapper, times(2)).toDto(any(Plant.class));
    }

    @Test
    void findById_WhenPlantExists_ShouldReturnOptionalPlantDTO() {
        when(plantRepository.findById(plant1Uuid)).thenReturn(Optional.of(plant1));
        when(plantMapper.toDto(plant1)).thenReturn(plantDTO1);

        Optional<PlantDTO> result = plantService.findById(plant1Uuid);

        assertTrue(result.isPresent());
        assertEquals(plantDTO1, result.get());
        verify(plantRepository).findById(plant1Uuid);
        verify(plantMapper).toDto(plant1);
    }

    @Test
    void findById_WhenPlantDoesNotExist_ShouldReturnEmptyOptional() {
        UUID nonExistentUuid = UUID.randomUUID();
        when(plantRepository.findById(nonExistentUuid)).thenReturn(Optional.empty());

        Optional<PlantDTO> result = plantService.findById(nonExistentUuid);

        assertTrue(result.isEmpty());
        verify(plantRepository).findById(nonExistentUuid);
        verify(plantMapper, never()).toDto(any());
    }

    @Test
    void create_ShouldReturnCreatedPlantDTO() {
        PlantDTO requestDto = PlantDTO.builder()
                .gardenPlanId(gardenPlanUuid)
                .name("New Plant")
                .type("Type")
                .description("New Desc")
                .build();
        Plant plantToSave = new Plant();
        plantToSave.setGardenPlanId(requestDto.getGardenPlanId());
        plantToSave.setName(requestDto.getName());
        plantToSave.setType(requestDto.getType());
        plantToSave.setDescription(requestDto.getDescription());
        plantToSave.setCreatedAt(OffsetDateTime.now());
        plantToSave.setUpdatedAt(OffsetDateTime.now());

        Plant savedPlant = new Plant();
        savedPlant.setId(UUID.randomUUID());
        savedPlant.setGardenPlanId(requestDto.getGardenPlanId());
        savedPlant.setName(requestDto.getName());
        savedPlant.setType(requestDto.getType());
        savedPlant.setDescription(requestDto.getDescription());
        savedPlant.setCreatedAt(OffsetDateTime.now());
        savedPlant.setUpdatedAt(OffsetDateTime.now());

        PlantDTO responseDto = PlantDTO.builder()
                .gardenPlanId(savedPlant.getGardenPlanId())
                .name(savedPlant.getName())
                .type(savedPlant.getType())
                .description(savedPlant.getDescription())
                .createdAt(savedPlant.getCreatedAt())
                .updatedAt(savedPlant.getUpdatedAt())
                .build();

        when(plantMapper.toEntity(requestDto)).thenReturn(plantToSave);
        when(plantRepository.save(any(Plant.class))).thenReturn(savedPlant);
        when(plantMapper.toDto(savedPlant)).thenReturn(responseDto);

        PlantDTO result = plantService.create(requestDto);

        assertNotNull(result);
        assertEquals(responseDto.getName(), result.getName());
        assertEquals(responseDto.getGardenPlanId(), result.getGardenPlanId());
        verify(plantMapper).toEntity(requestDto);
        verify(plantRepository).save(plantToSave);
        verify(plantMapper).toDto(savedPlant);
    }

    @Test
    void update_WhenPlantExists_ShouldReturnUpdatedOptionalPlantDTO() {
        PlantDTO updateRequestDto = PlantDTO.builder()
                .name("Updated Name")
                .type("Updated Type")
                .description("Updated Desc")
                .gardenPlanId(plant1.getGardenPlanId())
                .build();

        Plant existingPlant = plant1;

        Plant expectedSavedPlant = new Plant();
        expectedSavedPlant.setId(existingPlant.getId());
        expectedSavedPlant.setGardenPlanId(existingPlant.getGardenPlanId());
        expectedSavedPlant.setName(updateRequestDto.getName());
        expectedSavedPlant.setType(updateRequestDto.getType());
        expectedSavedPlant.setDescription(updateRequestDto.getDescription());
        expectedSavedPlant.setWateringFrequency(existingPlant.getWateringFrequency());
        expectedSavedPlant.setSunlightRequirements(existingPlant.getSunlightRequirements());
        expectedSavedPlant.setPositionX(existingPlant.getPositionX());
        expectedSavedPlant.setPositionY(existingPlant.getPositionY());
        expectedSavedPlant.setImageUrl(existingPlant.getImageUrl());
        expectedSavedPlant.setCreatedAt(existingPlant.getCreatedAt());

        Plant returnedSavedPlant = new Plant();
        returnedSavedPlant.setId(expectedSavedPlant.getId());
        returnedSavedPlant.setGardenPlanId(expectedSavedPlant.getGardenPlanId());
        returnedSavedPlant.setName(expectedSavedPlant.getName());
        returnedSavedPlant.setType(expectedSavedPlant.getType());
        returnedSavedPlant.setDescription(expectedSavedPlant.getDescription());
        returnedSavedPlant.setWateringFrequency(expectedSavedPlant.getWateringFrequency());
        returnedSavedPlant.setSunlightRequirements(expectedSavedPlant.getSunlightRequirements());
        returnedSavedPlant.setPositionX(expectedSavedPlant.getPositionX());
        returnedSavedPlant.setPositionY(expectedSavedPlant.getPositionY());
        returnedSavedPlant.setImageUrl(expectedSavedPlant.getImageUrl());
        returnedSavedPlant.setCreatedAt(expectedSavedPlant.getCreatedAt());
        returnedSavedPlant.setUpdatedAt(OffsetDateTime.now().plusSeconds(1));

        PlantDTO responseDto = PlantDTO.builder()
                .gardenPlanId(returnedSavedPlant.getGardenPlanId())
                .name(returnedSavedPlant.getName())
                .type(returnedSavedPlant.getType())
                .description(returnedSavedPlant.getDescription())
                .wateringFrequency(returnedSavedPlant.getWateringFrequency())
                .sunlightRequirements(returnedSavedPlant.getSunlightRequirements())
                .positionX(returnedSavedPlant.getPositionX())
                .positionY(returnedSavedPlant.getPositionY())
                .imageUrl(returnedSavedPlant.getImageUrl())
                .createdAt(returnedSavedPlant.getCreatedAt())
                .updatedAt(returnedSavedPlant.getUpdatedAt())
                .build();

        when(plantRepository.findById(plant1Uuid)).thenReturn(Optional.of(existingPlant));
        when(plantRepository.save(any(Plant.class))).thenReturn(returnedSavedPlant);
        when(plantMapper.toDto(returnedSavedPlant)).thenReturn(responseDto);

        Optional<PlantDTO> result = plantService.update(plant1Uuid, updateRequestDto);

        assertTrue(result.isPresent());
        assertEquals(responseDto, result.get());
        verify(plantRepository).findById(plant1Uuid);
        verify(plantRepository).save(existingPlant);
        verify(plantMapper).toDto(returnedSavedPlant);
    }

    @Test
    void update_WhenPlantDoesNotExist_ShouldReturnEmptyOptional() {
        UUID nonExistentUuid = UUID.randomUUID();
        PlantDTO updateRequestDto = PlantDTO.builder().name("Fail").build();
        when(plantRepository.findById(nonExistentUuid)).thenReturn(Optional.empty());

        Optional<PlantDTO> result = plantService.update(nonExistentUuid, updateRequestDto);

        assertTrue(result.isEmpty());
        verify(plantRepository).findById(nonExistentUuid);
        verify(plantRepository, never()).save(any());
    }

    @Test
    void delete_WhenPlantExists_ShouldDeletePlant() {
        doNothing().when(plantRepository).deleteById(plant1Uuid);

        plantService.delete(plant1Uuid);

        verify(plantRepository).deleteById(plant1Uuid);
    }

    @Test
    void searchPlants_ShouldReturnMatchingPlantDTOs() {
        String query = "Tomato";
        UUID specificGardenPlanId = gardenPlanUuid;
        Pageable pageable = PageRequest.of(0, 5);
        Page<Plant> plantPage = new PageImpl<>(List.of(plant1), pageable, 1);

        when(plantRepository.searchPlants(query, specificGardenPlanId, pageable))
                .thenReturn(plantPage);
        when(plantMapper.toDto(plant1)).thenReturn(plantDTO1);

        Page<PlantDTO> result = plantService.searchPlants(query, specificGardenPlanId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(plantDTO1, result.getContent().get(0));
        verify(plantRepository).searchPlants(query, specificGardenPlanId, pageable);
        verify(plantMapper).toDto(plant1);
    }

    @Test
    void searchPlants_WithNullGardenPlanId_ShouldReturnMatchingPlantDTOs() {
        String query = "Tomato";
        Pageable pageable = PageRequest.of(0, 5);
        Page<Plant> plantPage = new PageImpl<>(List.of(plant1), pageable, 1);

        when(plantRepository.searchPlants(eq(query), isNull(), eq(pageable))).thenReturn(plantPage);
        when(plantMapper.toDto(plant1)).thenReturn(plantDTO1);

        Page<PlantDTO> result = plantService.searchPlants(query, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(plantDTO1, result.getContent().get(0));
        verify(plantRepository).searchPlants(eq(query), isNull(), eq(pageable));
        verify(plantMapper).toDto(plant1);
    }
}
