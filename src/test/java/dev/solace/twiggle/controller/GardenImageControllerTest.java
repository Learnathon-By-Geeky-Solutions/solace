package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.*;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
        dto = GardenImageDTO.builder()
                .gardenPlanId(UUID.randomUUID())
                .imageUrl("https://example.com/image.jpg")
                .title("My Garden")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllGardenImages() throws Exception {
        Page<GardenImageDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenImageService.findAll(any(Pageable.class))).thenReturn(page);

        MockHttpServletRequestBuilder request = get("/api/garden-images");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testGetAllGardenImagesWithoutPagination() throws Exception {
        Mockito.when(gardenImageService.findAll()).thenReturn(List.of(dto));

        MockHttpServletRequestBuilder request = get("/api/garden-images/all");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetGardenImageById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.findById(id)).thenReturn(Optional.of(dto));

        MockHttpServletRequestBuilder request = get("/api/garden-images/{id}", id);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/image.jpg"));
    }

    @Test
    void testGetGardenImageById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.findById(id)).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder request = get("/api/garden-images/{id}", id);

        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    void testSearchImagesByTitle() throws Exception {
        Page<GardenImageDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenImageService.searchByTitle(eq("garden"), any(Pageable.class)))
                .thenReturn(page);

        MockHttpServletRequestBuilder request = get("/api/garden-images/search").param("title", "garden");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("My Garden"));
    }

    @Test
    void testCreateGardenImage() throws Exception {
        Mockito.when(gardenImageService.create(any())).thenReturn(dto);

        MockHttpServletRequestBuilder request = post("/api/garden-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden image created successfully"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/image.jpg"));
    }

    @Test
    void testUpdateGardenImage_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.update(eq(id), any())).thenReturn(Optional.of(dto));

        MockHttpServletRequestBuilder request = put("/api/garden-images/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden image updated successfully"))
                .andExpect(jsonPath("$.data.title").value("My Garden"));
    }

    @Test
    void testUpdateGardenImage_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenImageService.update(eq(id), any())).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder request = put("/api/garden-images/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    void testDeleteGardenImage() throws Exception {
        UUID id = UUID.randomUUID();

        MockHttpServletRequestBuilder request = delete("/api/garden-images/{id}", id);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden image deleted successfully"));
    }
}
