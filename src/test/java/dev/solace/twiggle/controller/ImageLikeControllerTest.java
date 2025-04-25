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

    @BeforeEach
    void setUp() {
        dto = new ImageLikeDTO(UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now());
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
    void testCreateImageLike() throws Exception {
        Mockito.when(service.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/image-likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(dto.getUserId().toString()));
    }

    @Test
    void testToggleLike() throws Exception {
        UUID imageId = dto.getImageId();
        UUID userId = dto.getUserId();
        Mockito.when(service.toggleLike(imageId, userId)).thenReturn(true);

        mockMvc.perform(post("/api/image-likes/image/" + imageId + "/user/" + userId + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testUnlikeImage() throws Exception {
        UUID imageId = dto.getImageId();
        UUID userId = dto.getUserId();
        Mockito.when(service.unlikeImage(imageId, userId)).thenReturn(true);

        mockMvc.perform(delete("/api/image-likes/image/" + imageId + "/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testCountLikesByImageId() throws Exception {
        Mockito.when(service.countByImageId(dto.getImageId())).thenReturn(5L);

        mockMvc.perform(get("/api/image-likes/image/" + dto.getImageId() + "/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void testHasUserLikedImage() throws Exception {
        Mockito.when(service.hasUserLikedImage(dto.getImageId(), dto.getUserId()))
                .thenReturn(true);

        mockMvc.perform(get("/api/image-likes/image/" + dto.getImageId() + "/user/" + dto.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testDeleteImageLike() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/image-likes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image like deleted successfully"));
    }
}
