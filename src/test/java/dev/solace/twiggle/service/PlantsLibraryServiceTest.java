package dev.solace.twiggle.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.PlantsLibraryDTO;
import dev.solace.twiggle.dto.PlantsLibrarySearchCriteria;
import dev.solace.twiggle.mapper.PlantsLibraryMapper;
import dev.solace.twiggle.model.PlantsLibrary;
import dev.solace.twiggle.repository.PlantsLibraryRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PlantsLibraryServiceTest {

    @Mock
    private PlantsLibraryRepository repo;

    @Mock
    private PlantsLibraryMapper mapper;

    @InjectMocks
    private PlantsLibraryService service;

    private PlantsLibrary entity;
    private PlantsLibraryDTO dto;

    @BeforeEach
    void setUp() {
        entity = PlantsLibrary.builder()
                .id(UUID.randomUUID())
                .commonName("Snake Plant")
                .createdAt(OffsetDateTime.now())
                .build();

        dto = PlantsLibraryDTO.builder()
                .id(entity.getId())
                .commonName(entity.getCommonName())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Test
    void findAll_shouldReturnPageOfDtos() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("commonName"));
        when(repo.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<PlantsLibraryDTO> result = service.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        verify(repo).findAll(pageable);
    }

    @Test
    void findById_whenPresent_returnsDto() {
        when(repo.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<PlantsLibraryDTO> result = service.findById(entity.getId());

        assertThat(result).contains(dto);
    }

    @Test
    void create_persistsAndReturnsDto() {
        when(mapper.toEntity(any(PlantsLibraryDTO.class))).thenReturn(entity);
        when(repo.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        PlantsLibraryDTO saved = service.create(
                PlantsLibraryDTO.builder().commonName("Snake Plant").build());

        assertThat(saved.getId()).isEqualTo(entity.getId());
        verify(repo).save(entity);
    }

    @Test
    void update_whenPlantExists_updatesAndReturnsDto() {
        PlantsLibraryDTO updatePayload =
                PlantsLibraryDTO.builder().commonName("Mother-in-Law's Tongue").build();

        // 1. Stub findById to return the existing entity
        when(repo.findById(entity.getId())).thenReturn(Optional.of(entity));

        // 2. Stub any side-effect method
        when(mapper.createTemperatureRange(updatePayload)).thenReturn(null);

        // 3. Stub save to return the updated entity (so mapper.toDto gets non-null)
        when(repo.save(entity)).thenReturn(entity);

        // 4. Leniently stub mapper.toDto for any non-null entity
        Mockito.lenient().when(mapper.toDto(any(PlantsLibrary.class))).thenReturn(updatePayload);

        Optional<PlantsLibraryDTO> updated = service.update(entity.getId(), updatePayload);

        assertThat(updated).contains(updatePayload);
        verify(repo).save(entity);
        assertThat(entity.getCommonName()).isEqualTo("Mother-in-Law's Tongue");
    }

    @Test
    void delete_deletesById() {
        service.delete(entity.getId());
        verify(repo).deleteById(entity.getId());
    }

    @Test
    void searchPlantsAdvanced_buildsSpecificationAndDelegatesToRepo() {
        PlantsLibrarySearchCriteria criteria = PlantsLibrarySearchCriteria.builder()
                .plantType("Succulent")
                .flower(true)
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        when(repo.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<PlantsLibraryDTO> result = service.searchPlantsAdvanced(criteria, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repo).findAll(any(Specification.class), eq(pageable));
    }
}
