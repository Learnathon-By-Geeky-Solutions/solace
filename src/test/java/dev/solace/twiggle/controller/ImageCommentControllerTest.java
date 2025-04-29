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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageCommentController.class)
@Import(ImageCommentControllerTest.ImageCommentTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class ImageCommentControllerTest {

    @TestConfiguration
    static class ImageCommentTestConfig {
        @Bean
        @Primary
        public ImageCommentService imageCommentService() {
            return Mockito.mock(ImageCommentService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
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

        // Reset the mock before each test
        Mockito.reset(imageCommentService);
    }

    @Test
    void testGetAllImageComments() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findAll(any(Pageable.class))).thenReturn(page);

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
        Mockito.when(imageCommentService.findByImageId(any(UUID.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/image-comments/image/" + dto.getImageId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Great image!"));
    }

    @Test
    void testGetCommentsByUserId() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findByUserId(any(UUID.class), any(Pageable.class)))
                .thenReturn(page);

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
        Mockito.when(imageCommentService.create(any(ImageCommentDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/image-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image comment created successfully"));
    }

    @Test
    void testUpdateImageComment_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(imageCommentService.update(eq(id), any(ImageCommentDTO.class)))
                .thenReturn(Optional.of(dto));

        mockMvc.perform(put("/api/image-comments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image comment updated successfully"));
    }

    @Test
    void testUpdateImageComment_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(imageCommentService.update(eq(id), any(ImageCommentDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/image-comments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteImageComment() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doNothing().when(imageCommentService).delete(id);

        mockMvc.perform(delete("/api/image-comments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image comment deleted successfully"));
    }

    @Test
    void testGetAllImageComments_withInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/image-comments").param("direction", "INVALID"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetAllImageComments_withServiceException() throws Exception {
        // Use explicit RuntimeException with a message
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.when(imageCommentService.findAll(any(Pageable.class))).thenThrow(exception);

        mockMvc.perform(get("/api/image-comments"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetCommentsByImageId_withInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/image-comments/image/" + dto.getImageId()).param("direction", "INVALID"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetCommentsByImageId_withServiceException() throws Exception {
        // Use explicit RuntimeException with a message
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.when(imageCommentService.findByImageId(any(UUID.class), any(Pageable.class)))
                .thenThrow(exception);

        mockMvc.perform(get("/api/image-comments/image/" + dto.getImageId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetCommentsByUserId_withInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/image-comments/user/" + dto.getUserId()).param("direction", "INVALID"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetCommentsByUserId_withServiceException() throws Exception {
        // Use explicit RuntimeException with a message
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.when(imageCommentService.findByUserId(any(UUID.class), any(Pageable.class)))
                .thenThrow(exception);

        mockMvc.perform(get("/api/image-comments/user/" + dto.getUserId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testCountCommentsByImageId_withServiceException() throws Exception {
        // Use explicit RuntimeException with a message
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.when(imageCommentService.countByImageId(any(UUID.class))).thenThrow(exception);

        mockMvc.perform(get("/api/image-comments/image/" + dto.getImageId() + "/count"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testCreateImageComment_withInvalidData() throws Exception {
        ImageCommentDTO invalidDto = ImageCommentDTO.builder().build(); // Missing required fields

        mockMvc.perform(post("/api/image-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateImageComment_withServiceException() throws Exception {
        // Use explicit RuntimeException with a message
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.when(imageCommentService.create(any(ImageCommentDTO.class))).thenThrow(exception);

        mockMvc.perform(post("/api/image-comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testUpdateImageComment_withInvalidData() throws Exception {
        UUID id = UUID.randomUUID();
        ImageCommentDTO invalidDto = ImageCommentDTO.builder().build(); // Missing required fields

        mockMvc.perform(put("/api/image-comments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateImageComment_withServiceException() throws Exception {
        UUID id = UUID.randomUUID();
        // Use explicit RuntimeException with a message
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.when(imageCommentService.update(eq(id), any(ImageCommentDTO.class)))
                .thenThrow(exception);

        mockMvc.perform(put("/api/image-comments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testDeleteImageComment_withServiceException() throws Exception {
        UUID id = UUID.randomUUID();
        // Use explicit RuntimeException with a message
        RuntimeException exception = new RuntimeException("Database error");
        Mockito.doThrow(exception).when(imageCommentService).delete(id);

        mockMvc.perform(delete("/api/image-comments/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetAllImageComments_withCustomPagination() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/image-comments")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "comment")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Great image!"));
    }

    @Test
    void testGetCommentsByImageId_withCustomPagination() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findByImageId(any(UUID.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/image-comments/image/" + dto.getImageId())
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "comment")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Great image!"));
    }

    @Test
    void testGetCommentsByUserId_withCustomPagination() throws Exception {
        Page<ImageCommentDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(imageCommentService.findByUserId(any(UUID.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/image-comments/user/" + dto.getUserId())
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "comment")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Great image!"));
    }
}
