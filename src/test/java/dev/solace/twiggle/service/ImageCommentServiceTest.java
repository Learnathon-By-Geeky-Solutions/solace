package dev.solace.twiggle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.ImageCommentDTO;
import dev.solace.twiggle.mapper.ImageCommentMapper;
import dev.solace.twiggle.model.ImageComment;
import dev.solace.twiggle.repository.ImageCommentRepository;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class ImageCommentServiceTest {

    @Mock
    private ImageCommentRepository repository;

    @Mock
    private ImageCommentMapper mapper;

    @InjectMocks
    private ImageCommentService service;

    private ImageComment entity;
    private ImageCommentDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UUID imageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        entity = new ImageComment(UUID.randomUUID(), imageId, userId, "Great!", OffsetDateTime.now());
        dto = new ImageCommentDTO(imageId, userId, "Great!", OffsetDateTime.now());
    }

    @Test
    void findAll_shouldReturnPageOfDTOs() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<ImageCommentDTO> result = service.findAll(PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_shouldReturnDTO() {
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<ImageCommentDTO> result = service.findById(entity.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findByImageId_shouldReturnPage() {
        when(repository.findByImageId(eq(entity.getImageId()), any())).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<ImageCommentDTO> result = service.findByImageId(entity.getImageId(), PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUserId_shouldReturnPage() {
        when(repository.findByUserId(eq(entity.getUserId()), any())).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<ImageCommentDTO> result = service.findByUserId(entity.getUserId(), PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
    }

    @Test
    void countByImageId_shouldReturnCount() {
        when(repository.countByImageId(entity.getImageId())).thenReturn(5L);

        long count = service.countByImageId(entity.getImageId());

        assertThat(count).isEqualTo(5);
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        ImageCommentDTO result = service.create(dto);

        assertThat(result.getComment()).isEqualTo("Great!");
    }

    @Test
    void update_shouldReturnUpdatedDTO() {
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<ImageCommentDTO> result = service.update(entity.getId(), dto);

        assertThat(result).isPresent();
    }

    @Test
    void update_shouldReturnEmptyIfNotFound() {
        UUID randomId = UUID.randomUUID();
        when(repository.findById(randomId)).thenReturn(Optional.empty());

        Optional<ImageCommentDTO> result = service.update(randomId, dto);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_shouldInvokeRepository() {
        UUID id = UUID.randomUUID();
        service.delete(id);
        verify(repository).deleteById(id);
    }
}
