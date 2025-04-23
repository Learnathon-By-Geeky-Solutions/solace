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
    void testFindById() {
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        Optional<ImageLikeDTO> result = service.findById(entity.getId());
        assertThat(result).isPresent();
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
    void testUnlikeImage_whenExists() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(true);

        boolean result = service.unlikeImage(imageId, userId);
        assertThat(result).isTrue();
    }

    @Test
    void testUnlikeImage_whenNotExists() {
        when(repository.existsByImageIdAndUserId(imageId, userId)).thenReturn(false);

        boolean result = service.unlikeImage(imageId, userId);
        assertThat(result).isFalse();
    }
}
