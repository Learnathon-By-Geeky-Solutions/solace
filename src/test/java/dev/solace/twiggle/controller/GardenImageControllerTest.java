package dev.solace.twiggle.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.GardenImageDTO;
import dev.solace.twiggle.service.GardenImageService;
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

@WebMvcTest(GardenImageController.class)
@Import(GardenImageControllerTest.GardenImageTestConfig.class)
class GardenImageControllerTest {

    @TestConfiguration
    static class GardenImageTestConfig {
        @Bean
        @Primary
        public GardenImageService gardenImageService() {
            return Mockito.mock(GardenImageService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GardenImageService gardenImageService;

    @Autowired
    private ObjectMapper objectMapper;

    private GardenImageDTO dto;

    @BeforeEach
    void setUp() {
        // make sure no stubbing from a previous test survives
        reset(gardenImageService);

        UUID gardenPlanId = UUID.randomUUID();
        dto = GardenImageDTO.builder()
                .gardenPlanId(gardenPlanId)
                .imageUrl("https://example.com/image.jpg")
                .title("My Garden")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllGardenImages() throws Exception {
        Page<GardenImageDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenImageService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/garden-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].gardenPlanId")
                        .value(dto.getGardenPlanId().toString()));
    }

    @Test
    void testGetAllGardenImagesWithoutPagination() throws Exception {
        Mockito.when(gardenImageService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/garden-images/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].gardenPlanId")
                        .value(dto.getGardenPlanId().toString()));
    }

    @Test
    void testGetGardenImageById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.findById(id)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/garden-images/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl").value(dto.getImageUrl()))
                .andExpect(jsonPath("$.data.gardenPlanId")
                        .value(dto.getGardenPlanId().toString()));
    }

    @Test
    void testGetGardenImageById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/garden-images/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void testSearchImagesByTitle() throws Exception {
        Page<GardenImageDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenImageService.searchByTitle(Mockito.eq("garden"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/garden-images/search").param("title", "garden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("My Garden"))
                .andExpect(jsonPath("$.data.content[0].gardenPlanId")
                        .value(dto.getGardenPlanId().toString()));
    }

    @Test
    void testCreateGardenImage() throws Exception {
        GardenImageDTO inputDto = GardenImageDTO.builder()
                .gardenPlanId(UUID.randomUUID())
                .imageUrl("https://example.com/image.jpg")
                .title("My Garden")
                .build();

        GardenImageDTO responseDto = GardenImageDTO.builder()
                .gardenPlanId(inputDto.getGardenPlanId())
                .imageUrl(inputDto.getImageUrl())
                .title(inputDto.getTitle())
                .createdAt(OffsetDateTime.now())
                .build();

        Mockito.when(gardenImageService.create(any(GardenImageDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/garden-images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden image created successfully"))
                .andExpect(jsonPath("$.data.imageUrl").value(inputDto.getImageUrl()))
                .andExpect(jsonPath("$.data.gardenPlanId")
                        .value(inputDto.getGardenPlanId().toString()))
                .andExpect(jsonPath("$.data.title").value(inputDto.getTitle()));
    }

    @Test
    void testUpdateGardenImage_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.update(Mockito.eq(id), any())).thenReturn(Optional.of(dto));

        mockMvc.perform(put("/api/garden-images/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden image updated successfully"))
                .andExpect(jsonPath("$.data.title").value("My Garden"));
    }

    @Test
    void testUpdateGardenImage_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.update(Mockito.eq(id), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/garden-images/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteGardenImage() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/garden-images/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden image deleted successfully"));
    }

    @Test
    void testGetImagesByGardenPlanId() throws Exception {
        Page<GardenImageDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenImageService.findByGardenPlanId(Mockito.eq(dto.getGardenPlanId()), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/garden-images/garden-plan/{gardenPlanId}", dto.getGardenPlanId())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].gardenPlanId")
                        .value(dto.getGardenPlanId().toString()));
    }

    /* ----------  Validation / error-path tests ---------- */

    @Test
    void testCreateGardenImageWithInvalidData() throws Exception {
        GardenImageDTO invalidDto = GardenImageDTO.builder().build(); // missing required fields

        mockMvc.perform(post("/api/garden-images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Validation failed. Please check the provided data.")));
    }

    @Test
    void testUpdateGardenImageWithInvalidData() throws Exception {
        UUID id = UUID.randomUUID();
        GardenImageDTO invalidDto = GardenImageDTO.builder().build();

        mockMvc.perform(put("/api/garden-images/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Validation failed. Please check the provided data.")));
    }

    @Test
    void testGetAllGardenImagesWithInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/garden-images").param("direction", "INVALID"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSearchImagesByTitleWithInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/garden-images/search")
                        .param("title", "garden")
                        .param("direction", "INVALID"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteGardenImageWhenServiceThrowsException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new RuntimeException("Service error"))
                .when(gardenImageService)
                .delete(id);

        mockMvc.perform(delete("/api/garden-images/{id}", id)).andExpect(status().isInternalServerError());
    }

    @Test
    void testGetGardenImageByIdWhenServiceThrowsException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.findById(id)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/garden-images/{id}", id)).andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateGardenImageWhenServiceThrowsException() throws Exception {
        GardenImageDTO inputDto = GardenImageDTO.builder()
                .gardenPlanId(UUID.randomUUID())
                .imageUrl("https://example.com/image.jpg")
                .title("My Garden")
                .build();

        Mockito.when(gardenImageService.create(any(GardenImageDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/garden-images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateGardenImageWhenServiceThrowsException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.update(Mockito.eq(id), any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/api/garden-images/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }
}
