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

    @Test
    void findAll_shouldReturnEmptyPageWhenNoData() {
        when(repository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<GardenPlanDTO> result = service.findAll(PageRequest.of(0, 10));

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<GardenPlanDTO> result = service.findById(id);

        assertThat(result).isEmpty();
        verify(mapper, never()).toDto(any());
    }

    @Test
    void create_shouldGenerateNewId() {
        GardenPlanDTO inputDto = GardenPlanDTO.builder()
                .userId(UUID.randomUUID())
                .name("New Garden")
                .type("Indoor")
                .description("Test description")
                .location("Test location")
                .thumbnailUrl("https://test.com")
                .isPublic(false)
                .build();

        GardenPlan newEntity = new GardenPlan(
                null, // ID should be generated
                inputDto.getUserId(),
                inputDto.getName(),
                inputDto.getType(),
                inputDto.getDescription(),
                inputDto.getLocation(),
                inputDto.getThumbnailUrl(),
                inputDto.getIsPublic(),
                null,
                null);

        when(mapper.toEntity(inputDto)).thenReturn(newEntity);
        when(repository.save(any())).thenReturn(newEntity);
        when(mapper.toDto(any())).thenReturn(inputDto);

        GardenPlanDTO result = service.create(inputDto);

        assertThat(result).isNotNull();
        verify(repository).save(any());
    }

    @Test
    void update_shouldPreserveExistingId() {
        UUID existingId = UUID.randomUUID();
        GardenPlan existingEntity = new GardenPlan(
                existingId,
                UUID.randomUUID(),
                "Old Name",
                "Old Type",
                "Old Description",
                "Old Location",
                "https://old.com",
                false,
                OffsetDateTime.now(),
                OffsetDateTime.now());

        GardenPlanDTO updateDto = GardenPlanDTO.builder()
                .userId(existingEntity.getUserId())
                .name("Updated Name")
                .type("Updated Type")
                .description("Updated Description")
                .location("Updated Location")
                .thumbnailUrl("https://updated.com")
                .isPublic(true)
                .build();

        when(repository.findById(existingId)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any())).thenReturn(existingEntity);
        when(mapper.toDto(any())).thenReturn(updateDto);

        Optional<GardenPlanDTO> result = service.update(existingId, updateDto);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Updated Name");
        verify(repository).save(argThat(e -> e.getId().equals(existingId)));
    }

    @Test
    void delete_shouldNotThrowExceptionWhenIdNotFound() {
        UUID id = UUID.randomUUID();
        doNothing().when(repository).deleteById(id);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void findAll_shouldApplySorting() {
        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);

        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<GardenPlanDTO> result = service.findAll(pageable);

        assertThat(result).hasSize(1);
        verify(repository).findAll(pageable);
    }

    @Test
    void findAll_shouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<GardenPlanDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Urban Garden");
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoData() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<GardenPlanDTO> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_shouldReturnPage() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // Create a new entity with the specific userId for this test
        GardenPlan testEntity = new GardenPlan(
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

        // Create a new DTO with the specific userId for this test
        GardenPlanDTO testDto = GardenPlanDTO.builder()
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

        when(repository.findByUserId(userId, pageable)).thenReturn(new PageImpl<>(List.of(testEntity)));
        when(mapper.toDto(testEntity)).thenReturn(testDto);

        Page<GardenPlanDTO> result = service.findByUserId(userId, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo(userId);
    }

    @Test
    void findByUserId_shouldReturnList() {
        UUID userId = UUID.randomUUID();

        // Create a new entity with the specific userId for this test
        GardenPlan testEntity = new GardenPlan(
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

        // Create a new DTO with the specific userId for this test
        GardenPlanDTO testDto = GardenPlanDTO.builder()
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

        when(repository.findByUserId(userId)).thenReturn(List.of(testEntity));
        when(mapper.toDto(testEntity)).thenReturn(testDto);

        List<GardenPlanDTO> result = service.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUserId()).isEqualTo(userId);
    }

    @Test
    void findPublicPlans_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findByIsPublicTrue(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<GardenPlanDTO> result = service.findPublicPlans(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getIsPublic()).isTrue();
    }

    @Test
    void findPublicPlans_shouldReturnList() {
        when(repository.findByIsPublicTrue()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<GardenPlanDTO> result = service.findPublicPlans();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getIsPublic()).isTrue();
    }

    @Test
    void searchGardenPlans_shouldReturnPage() {
        String query = "garden";
        UUID userId = UUID.randomUUID();
        Boolean isPublic = true;
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.searchGardenPlans(query, userId, isPublic, pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<GardenPlanDTO> result = service.searchGardenPlans(query, userId, isPublic, pageable);

        assertThat(result).hasSize(1);
        verify(repository).searchGardenPlans(query, userId, isPublic, pageable);
    }

    @Test
    void searchGardenPlansWithRelevance_shouldReturnPage() {
        String name = "garden";
        String type = "balcony";
        String location = "dhaka";
        String query = "urban";
        UUID userId = UUID.randomUUID();
        Boolean isPublic = true;
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.searchGardenPlansWithRelevance(name, type, location, query, userId, isPublic, pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<GardenPlanDTO> result =
                service.searchGardenPlansWithRelevance(name, type, location, query, userId, isPublic, pageable);

        assertThat(result).hasSize(1);
        verify(repository).searchGardenPlansWithRelevance(name, type, location, query, userId, isPublic, pageable);
    }

    @Test
    void searchGardenPlansWithRelevance_shouldFallbackToSimpleSearch() {
        String name = "garden";
        String type = "balcony";
        String location = "dhaka";
        String query = "urban";
        UUID userId = UUID.randomUUID();
        Boolean isPublic = true;
        Pageable pageable = PageRequest.of(0, 10);

        // Simulate an exception in the enhanced search
        when(repository.searchGardenPlansWithRelevance(name, type, location, query, userId, isPublic, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // Expect the fallback to be called
        when(repository.searchGardenPlans(query, userId, isPublic, pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<GardenPlanDTO> result =
                service.searchGardenPlansWithRelevance(name, type, location, query, userId, isPublic, pageable);

        assertThat(result).hasSize(1);
        verify(repository).searchGardenPlansWithRelevance(name, type, location, query, userId, isPublic, pageable);
        verify(repository).searchGardenPlans(query, userId, isPublic, pageable);
    }
}
