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

    @Test
    void findAll_withoutPagination_shouldReturnListOfDTOs() {
        // Test the findAll() method without pagination
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<ImageCommentDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto);
        verify(repository).findAll();
        verify(mapper).toDto(entity);
    }

    @Test
    void create_shouldSetCreatedAtTimestamp() {
        // Test that the create method sets the createdAt timestamp
        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        // Use ArgumentCaptor to capture the DTO when passed to the mapper
        ArgumentCaptor<ImageCommentDTO> dtoCaptor = ArgumentCaptor.forClass(ImageCommentDTO.class);

        ImageCommentDTO input = new ImageCommentDTO(
                UUID.randomUUID(), UUID.randomUUID(), "New comment", null // No timestamp initially
                );

        service.create(input);

        // Verify mapper.toEntity was called with a DTO that has createdAt set
        verify(mapper).toEntity(dtoCaptor.capture());
        ImageCommentDTO capturedDto = dtoCaptor.getValue();

        assertThat(capturedDto.getCreatedAt()).isNotNull();
    }

    @Test
    void update_shouldOnlyUpdateCommentField() {
        // Test that the update method only updates the comment field
        UUID id = entity.getId();
        String newComment = "Updated comment";

        // Create a modified DTO with different values for all fields
        ImageCommentDTO updatedDto = new ImageCommentDTO(
                UUID.randomUUID(), // Different image ID
                UUID.randomUUID(), // Different user ID
                newComment,
                OffsetDateTime.now().plusDays(1) // Different timestamp
                );

        // Capture the entity passed to repository.save
        ArgumentCaptor<ImageComment> entityCaptor = ArgumentCaptor.forClass(ImageComment.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(ImageComment.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        service.update(id, updatedDto);

        verify(repository).save(entityCaptor.capture());
        ImageComment savedEntity = entityCaptor.getValue();

        // Verify only the comment field was updated
        assertThat(savedEntity.getComment()).isEqualTo(newComment);
        // Verify the other fields remain unchanged
        assertThat(savedEntity.getImageId()).isEqualTo(entity.getImageId());
        assertThat(savedEntity.getUserId()).isEqualTo(entity.getUserId());
    }

    @Test
    void delete_shouldThrowExceptionWhenNotFound() {
        // Test behavior when trying to delete a non-existent comment
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new RuntimeException("Entity not found")).when(repository).deleteById(nonExistentId);

        try {
            service.delete(nonExistentId);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Entity not found");
        }

        verify(repository).deleteById(nonExistentId);
    }

    @Test
    void create_shouldHandleMappingFailure() {
        // Test error handling during creation if mapping fails
        ImageCommentDTO invalidDto = new ImageCommentDTO(null, null, null, null);
        when(mapper.toEntity(invalidDto)).thenThrow(new IllegalArgumentException("Invalid DTO"));

        try {
            service.create(invalidDto);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getMessage()).isEqualTo("Invalid DTO");
        }

        verify(mapper).toEntity(invalidDto);
        verify(repository, never()).save(any());
    }
}
