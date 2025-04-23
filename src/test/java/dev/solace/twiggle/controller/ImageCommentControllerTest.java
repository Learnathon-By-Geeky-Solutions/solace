package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.ImageCommentDTO;
import dev.solace.twiggle.service.ImageCommentService;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageCommentController.class)
class ImageCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImageCommentService imageCommentService;

    private ImageCommentDTO dto;

    @BeforeEach
    void setup() {
        dto = ImageCommentDTO.builder()
                .imageId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .comment("Great image!")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllImageComments() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/image-comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Great image!"));
    }

    @Test
    void testGetImageCommentById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(imageCommentService.findById(id)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/image-comments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comment").value("Great image!"));
    }

    @Test
    void testGetImageCommentById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(imageCommentService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/image-comments/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void testGetCommentsByImageId() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findByImageId(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/image-comments/image/" + dto.getImageId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Great image!"));
    }

    @Test
    void testGetCommentsByUserId() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findByUserId(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/image-comments/user/" + dto.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Great image!"));
    }

    @Test
    void testCountCommentsByImageId() throws Exception {
        Mockito.when(imageCommentService.countByImageId(dto.getImageId())).thenReturn(3L);

        mockMvc.perform(get("/api/image-comments/image/" + dto.getImageId() + "/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    void testCreateImageComment() throws Exception {
        Mockito.when(imageCommentService.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/image-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image comment created successfully"));
    }

    @Test
    void testUpdateImageComment_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(imageCommentService.update(eq(id), any())).thenReturn(Optional.of(dto));

        mockMvc.perform(put("/api/image-comments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image comment updated successfully"));
    }

    @Test
    void testUpdateImageComment_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(imageCommentService.update(eq(id), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/image-comments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteImageComment() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/image-comments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image comment deleted successfully"));
    }
}
