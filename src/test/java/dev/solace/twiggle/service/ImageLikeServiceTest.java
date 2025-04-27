package dev.solace.twiggle.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.solace.twiggle.dto.ImageLikeDTO;
import dev.solace.twiggle.mapper.ImageLikeMapper;
import dev.solace.twiggle.model.ImageLike;
import dev.solace.twiggle.repository.ImageLikeRepository;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

class ImageLikeServiceTest {

    @Mock
    private ImageLikeRepository repository;

    @Mock
    private ImageLikeMapper mapper;

    @InjectMocks
    private ImageLikeService service;

    private UUID imageId;
    private UUID userId;
    private ImageLike entity;
    private ImageLikeDTO dto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        imageId = UUID.randomUUID();
        userId = UUID.randomUUID();
        entity = new ImageLike(UUID.randomUUID(), imageId, userId, OffsetDateTime.now());
        dto = new ImageLikeDTO(imageId, userId, OffsetDateTime.now());
    }

    @Test
    void testFindAllPageable() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<ImageLikeDTO> result = service.findAll(PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
    }

    @Test
    void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<ImageLikeDTO> result = service.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(dto);
    }

    @Test
    void testFindById() {
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<ImageLikeDTO> result = service.findById(entity.getId());
        assertThat(result).isPresent();
    }

    @Test
    void testFindById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<ImageLikeDTO> result = service.findById(nonExistentId);
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByImageId() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByImageId(imageId, pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<ImageLikeDTO> result = service.findByImageId(imageId, pageable);
        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getImageId()).isEqualTo(imageId);
    }

    @Test
    void testFindByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findByUserId(userId, pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDto(entity)).thenReturn(dto);

        Page<ImageLikeDTO> result = service.findByUserId(userId, pageable);
        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getUserId()).isEqualTo(userId);
    }

    @Test
    void testFindByImageIdAndUserId() {
        when(repository.findByImageIdAndUserId(imageId, userId)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<ImageLikeDTO> result = service.findByImageIdAndUserId(imageId, userId);
        assertThat(result).isPresent();
        assertThat(result.get().getImageId()).isEqualTo(imageId);
        assertThat(result.get().getUserId()).isEqualTo(userId);
    }

    @Test
    void testFindByImageIdAndUserId_NotFound() {
        when(repository.findByImageIdAndUserId(imageId, userId)).thenReturn(Optional.empty());

        Optional<ImageLikeDTO> result = service.findByImageIdAndUserId(imageId, userId);
        assertThat(result).isEmpty();
    }

    @Test
    void testHasUserLikedImage_True() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(true);

        boolean result = service.hasUserLikedImage(imageId, userId);
        assertThat(result).isTrue();
    }

    @Test
    void testHasUserLikedImage_False() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(false);

        boolean result = service.hasUserLikedImage(imageId, userId);
        assertThat(result).isFalse();
    }

    @Test
    void testCountByImageId() {
        long expectedCount = 5L;
        when(repository.countByImageId(imageId)).thenReturn(expectedCount);

        long result = service.countByImageId(imageId);
        assertThat(result).isEqualTo(expectedCount);
    }

    @Test
    void testCreateNewLike() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        ImageLikeDTO result = service.create(dto);
        assertThat(result.getImageId()).isEqualTo(imageId);
    }

    @Test
    void testCreateNewLike_AlreadyExists() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already liked");
    }

    @Test
    void testToggleLike_whenNotLiked() {
        when(repository.findByImageIdAndUserId(imageId, userId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(entity);

        boolean result = service.toggleLike(imageId, userId);
        assertThat(result).isTrue();
    }

    @Test
    void testToggleLike_whenAlreadyLiked() {
        when(repository.findByImageIdAndUserId(imageId, userId)).thenReturn(Optional.of(entity));

        boolean result = service.toggleLike(imageId, userId);
        assertThat(result).isFalse();
    }

    @Test
    void testDelete() {
        UUID id = UUID.randomUUID();

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void testUnlikeImage_whenExists() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(true);

        boolean result = service.unlikeImage(imageId, userId);
        assertThat(result).isTrue();
        verify(repository).deleteByImageIdAndUserId(imageId, userId);
    }

    @Test
    void testUnlikeImage_whenNotExists() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(false);

        boolean result = service.unlikeImage(imageId, userId);
        assertThat(result).isFalse();
        verify(repository, never()).deleteByImageIdAndUserId(any(), any());
    }
}
