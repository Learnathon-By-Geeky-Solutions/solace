package dev.solace.twiggle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.GardenPlanDTO;
import dev.solace.twiggle.mapper.GardenPlanMapper;
import dev.solace.twiggle.model.GardenPlan;
import dev.solace.twiggle.repository.GardenPlanRepository;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class GardenPlanServiceTest {

    @Mock
    private GardenPlanRepository repository;

    @Mock
    private GardenPlanMapper mapper;

    @InjectMocks
    private GardenPlanService service;

    private GardenPlan entity;
    private GardenPlanDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID userId = UUID.randomUUID();
        entity = new GardenPlan(
                UUID.randomUUID(),
                userId,
                "Urban Garden",
                "Balcony",
                "A setup",
                "Dhaka",
                "https://img.com",
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now());

        dto = GardenPlanDTO.builder()
                .userId(userId)
                .name("Urban Garden")
                .type("Balcony")
                .description("A setup")
                .location("Dhaka")
                .thumbnailUrl("https://img.com")
                .isPublic(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void findAll_shouldReturnPage() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<GardenPlanDTO> result = service.findAll(PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_shouldReturnDTO() {
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<GardenPlanDTO> result = service.findById(entity.getId());

        assertThat(result).isPresent();
    }

    @Test
    void update_shouldReturnUpdatedDTO() {
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(repository.save(any())).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<GardenPlanDTO> result = service.update(entity.getId(), dto);

        assertThat(result).isPresent();
    }

    @Test
    void update_shouldReturnEmptyIfNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<GardenPlanDTO> result = service.update(id, dto);

        assertThat(result).isEmpty();
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        GardenPlanDTO result = service.create(dto);

        assertThat(result.getName()).isEqualTo("Urban Garden");
    }

    @Test
    void delete_shouldInvokeRepository() {
        UUID id = UUID.randomUUID();
        service.delete(id);
        verify(repository).deleteById(id);
    }
}
