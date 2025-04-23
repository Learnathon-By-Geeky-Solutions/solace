package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.PestDTO;
import dev.solace.twiggle.mapper.PestMapper;
import dev.solace.twiggle.model.Pest;
import dev.solace.twiggle.model.PlantsLibrary;
import dev.solace.twiggle.repository.PestRepository;
import dev.solace.twiggle.repository.PlantsLibraryRepository;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PestServiceTest {

    @Mock
    private PestRepository pestRepository;

    @Mock
    private PlantsLibraryRepository plantsLibraryRepository;

    @Mock
    private PestMapper pestMapper;

    @InjectMocks
    private PestService pestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Pest pest = new Pest();
        PestDTO pestDTO = new PestDTO();

        when(pestRepository.findAll()).thenReturn(List.of(pest));
        when(pestMapper.toDto(pest)).thenReturn(pestDTO);

        List<PestDTO> result = pestService.findAll();

        assertEquals(1, result.size());
        verify(pestRepository, times(1)).findAll();
        verify(pestMapper, times(1)).toDto(pest);
    }

    @Test
    void testFindByPlantLibraryId_Found() {
        UUID plantId = UUID.randomUUID();

        PlantsLibrary plant = new PlantsLibrary();
        plant.setCommonPests(List.of("Aphid", "Mealybug"));

        Pest aphid = new Pest();
        PestDTO aphidDTO = new PestDTO();

        when(plantsLibraryRepository.findById(plantId)).thenReturn(Optional.of(plant));
        when(pestRepository.findByCommonNameIgnoreCaseIn(Set.of("aphid", "mealybug")))
                .thenReturn(List.of(aphid));
        when(pestMapper.toDto(aphid)).thenReturn(aphidDTO);

        List<PestDTO> result = pestService.findByPlantLibraryId(plantId);

        assertEquals(1, result.size());
        verify(plantsLibraryRepository, times(1)).findById(plantId);
        verify(pestRepository, times(1)).findByCommonNameIgnoreCaseIn(Set.of("aphid", "mealybug"));
        verify(pestMapper, times(1)).toDto(aphid);
    }

    @Test
    void testFindByPlantLibraryId_NotFound() {
        UUID plantId = UUID.randomUUID();
        when(plantsLibraryRepository.findById(plantId)).thenReturn(Optional.empty());

        List<PestDTO> result = pestService.findByPlantLibraryId(plantId);

        assertEquals(0, result.size());
        verify(plantsLibraryRepository, times(1)).findById(plantId);
        verifyNoMoreInteractions(pestRepository);
    }
}
