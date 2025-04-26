package dev.solace.twiggle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.GardenImageDTO;
import dev.solace.twiggle.mapper.GardenImageMapper;
import dev.solace.twiggle.model.GardenImage;
import dev.solace.twiggle.repository.GardenImageRepository;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class GardenImageServiceTest {

    @Mock
    private GardenImageRepository gardenImageRepository;

    @Mock
    private GardenImageMapper gardenImageMapper;

    @InjectMocks
    private GardenImageService gardenImageService;

    private GardenImage image;
    private GardenImageDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID planId = UUID.randomUUID();
        image = new GardenImage(
                UUID.randomUUID(), planId, "https://example.com/img.jpg", "My Garden", OffsetDateTime.now());
        dto = new GardenImageDTO(planId, "https://example.com/img.jpg", "My Garden", OffsetDateTime.now());
    }

    @Test
    void findAll_withPageable_shouldReturnPageOfDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        when(gardenImageRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(image)));
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        Page<GardenImageDTO> result = gardenImageService.findAll(pageable);

        assertThat(result).hasSize(1);
        verify(gardenImageRepository).findAll(pageable);
    }

    @Test
    void findAll_shouldReturnListOfDTOs() {
        when(gardenImageRepository.findAll()).thenReturn(List.of(image));
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        List<GardenImageDTO> result = gardenImageService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_shouldReturnOptionalDTO() {
        when(gardenImageRepository.findById(image.getId())).thenReturn(Optional.of(image));
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        Optional<GardenImageDTO> result = gardenImageService.findById(image.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findByGardenPlanId_shouldReturnList() {
        when(gardenImageRepository.findByGardenPlanId(image.getGardenPlanId())).thenReturn(List.of(image));
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        List<GardenImageDTO> result = gardenImageService.findByGardenPlanId(image.getGardenPlanId());

        assertThat(result).hasSize(1);
    }

    @Test
    void findByGardenPlanId_withPageable_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(gardenImageRepository.findByGardenPlanId(image.getGardenPlanId(), pageable))
                .thenReturn(new PageImpl<>(List.of(image)));
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        Page<GardenImageDTO> result = gardenImageService.findByGardenPlanId(image.getGardenPlanId(), pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByGardenPlanIdAndTitle_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(gardenImageRepository.findByGardenPlanIdAndTitleContainingIgnoreCase(
                        image.getGardenPlanId(), "garden", pageable))
                .thenReturn(new PageImpl<>(List.of(image)));
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        Page<GardenImageDTO> result =
                gardenImageService.findByGardenPlanIdAndTitle(image.getGardenPlanId(), "garden", pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchByTitle_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(gardenImageRepository.findByTitleContainingIgnoreCase("My", pageable))
                .thenReturn(new PageImpl<>(List.of(image)));
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        Page<GardenImageDTO> result = gardenImageService.searchByTitle("My", pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        when(gardenImageMapper.toEntity(any())).thenReturn(image);
        when(gardenImageRepository.save(image)).thenReturn(image);
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        GardenImageDTO result = gardenImageService.create(dto);

        assertThat(result).isNotNull();
        verify(gardenImageRepository).save(any());
    }

    @Test
    void update_shouldModifyAndReturnDTO_whenExists() {
        when(gardenImageRepository.findById(image.getId())).thenReturn(Optional.of(image));
        when(gardenImageRepository.save(image)).thenReturn(image);
        when(gardenImageMapper.toDto(image)).thenReturn(dto);

        Optional<GardenImageDTO> result = gardenImageService.update(image.getId(), dto);

        assertThat(result).isPresent();
        verify(gardenImageRepository).save(image);
    }

    @Test
    void update_shouldReturnEmpty_whenNotExists() {
        UUID randomId = UUID.randomUUID();
        when(gardenImageRepository.findById(randomId)).thenReturn(Optional.empty());

        Optional<GardenImageDTO> result = gardenImageService.update(randomId, dto);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_shouldCallRepository() {
        UUID id = UUID.randomUUID();
        gardenImageService.delete(id);
        verify(gardenImageRepository).deleteById(id);
    }
}
