package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.ImageLikeDTO;
import dev.solace.twiggle.service.ImageLikeService;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageLikeController.class)
@Import(ImageLikeControllerTest.ImageLikeTestConfig.class)
class ImageLikeControllerTest {

    @TestConfiguration
    static class ImageLikeTestConfig {
        @Bean
        @Primary
        public ImageLikeService imageLikeService() {
            return Mockito.mock(ImageLikeService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImageLikeService service;

    @Autowired
    private ObjectMapper mapper;

    private ImageLikeDTO dto;
    private UUID imageId;
    private UUID userId;
    private UUID likeId;

    @BeforeEach
    void setUp() {
        imageId = UUID.randomUUID();
        userId = UUID.randomUUID();
        likeId = UUID.randomUUID();
        dto = ImageLikeDTO.builder()
                .imageId(imageId)
                .userId(userId)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllImageLikes() throws Exception {
        Page<ImageLikeDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(service.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/image-likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].userId").exists());
    }

    @Test
    void testGetAllImageLikesWithPaginationAndSorting() throws Exception {
        Page<ImageLikeDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(service.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/image-likes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].userId").exists());
    }

    @Test
    void testGetAllImageLikesError() throws Exception {
        Mockito.when(service.findAll(any())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/image-likes"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetImageLikeById() throws Exception {
        Mockito.when(service.findById(likeId)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/image-likes/" + likeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(dto.getUserId().toString()));
    }

    @Test
    void testGetImageLikeByIdNotFound() throws Exception {
        Mockito.when(service.findById(likeId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/image-likes/" + likeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetImageLikeByIdError() throws Exception {
        Mockito.when(service.findById(likeId)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/image-likes/" + likeId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGetLikesByImageId() throws Exception {
        Page<ImageLikeDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(service.findByImageId(eq(imageId), any())).thenReturn(page);

        mockMvc.perform(get("/api/image-likes/image/" + imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].userId").exists());
    }

    @Test
    void testGetLikesByImageIdWithPaginationAndSorting() throws Exception {
        Page<ImageLikeDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(service.findByImageId(eq(imageId), any())).thenReturn(page);

        mockMvc.perform(get("/api/image-likes/image/" + imageId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].userId").exists());
    }

    @Test
    void testGetLikesByImageIdError() throws Exception {
        Mockito.when(service.findByImageId(eq(imageId), any())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/image-likes/image/" + imageId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCountLikesByImageId() throws Exception {
        Mockito.when(service.countByImageId(imageId)).thenReturn(5L);

        mockMvc.perform(get("/api/image-likes/image/" + imageId + "/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void testCountLikesByImageIdError() throws Exception {
        Mockito.when(service.countByImageId(imageId)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/image-likes/image/" + imageId + "/count"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testHasUserLikedImage() throws Exception {
        Mockito.when(service.hasUserLikedImage(imageId, userId)).thenReturn(true);

        mockMvc.perform(get("/api/image-likes/image/" + imageId + "/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testHasUserLikedImageFalse() throws Exception {
        Mockito.when(service.hasUserLikedImage(imageId, userId)).thenReturn(false);

        mockMvc.perform(get("/api/image-likes/image/" + imageId + "/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void testHasUserLikedImageError() throws Exception {
        Mockito.when(service.hasUserLikedImage(imageId, userId)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/image-likes/image/" + imageId + "/user/" + userId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateImageLike() throws Exception {
        // Reset the mock to ensure clean state
        Mockito.reset(service);
        // Mock the create method to return the DTO
        Mockito.when(service.create(any(ImageLikeDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/image-likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(dto.getUserId().toString()));
    }

    @Test
    void testCreateImageLikeDuplicate() throws Exception {
        // Reset the mock to ensure clean state
        Mockito.reset(service);
        // Mock the create method to throw IllegalStateException
        Mockito.when(service.create(any(ImageLikeDTO.class)))
                .thenThrow(new IllegalStateException("User has already liked this image"));

        mockMvc.perform(post("/api/image-likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateImageLikeError() throws Exception {
        // Reset the mock to ensure clean state
        Mockito.reset(service);
        // Mock the create method to throw RuntimeException
        Mockito.when(service.create(any(ImageLikeDTO.class))).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/image-likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateImageLikeValidationError() throws Exception {
        // Create an invalid DTO (missing required fields)
        ImageLikeDTO invalidDto = new ImageLikeDTO();

        mockMvc.perform(post("/api/image-likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testToggleLike() throws Exception {
        Mockito.when(service.toggleLike(imageId, userId)).thenReturn(true);

        mockMvc.perform(post("/api/image-likes/image/" + imageId + "/user/" + userId + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testToggleLikeError() throws Exception {
        Mockito.when(service.toggleLike(imageId, userId)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/image-likes/image/" + imageId + "/user/" + userId + "/toggle"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testUnlikeImage() throws Exception {
        Mockito.when(service.unlikeImage(imageId, userId)).thenReturn(true);

        mockMvc.perform(delete("/api/image-likes/image/" + imageId + "/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testUnlikeImageError() throws Exception {
        Mockito.when(service.unlikeImage(imageId, userId)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/image-likes/image/" + imageId + "/user/" + userId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDeleteImageLike() throws Exception {
        mockMvc.perform(delete("/api/image-likes/" + likeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image like deleted successfully"));
    }

    @Test
    void testDeleteImageLikeError() throws Exception {
        Mockito.doThrow(new RuntimeException("Database error")).when(service).delete(likeId);

        mockMvc.perform(delete("/api/image-likes/" + likeId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }
}
