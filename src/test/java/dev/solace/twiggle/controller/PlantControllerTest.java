package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.service.PlantService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantController.class)
@Import(PlantControllerTest.PlantTestConfig.class)
class PlantControllerTest {

    @TestConfiguration
    static class PlantTestConfig {
        @Bean
        @Primary
        public PlantService plantService() {
            return Mockito.mock(PlantService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlantService service;

    @Autowired
    private ObjectMapper objectMapper;

    private PlantDTO dto;

    @BeforeEach
    void setup() {
        dto = new PlantDTO(
                UUID.randomUUID(),
                "Basil",
                "Herb",
                "desc",
                "Daily",
                "Full Sun",
                1,
                2,
                "url",
                OffsetDateTime.now(),
                OffsetDateTime.now());
    }

    @Test
    void testGetAllPlants() throws Exception {
        Mockito.when(service.findAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testGetPlantById() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.findById(id)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/plants/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Basil"));
    }

    @Test
    void testGetPlantByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/plants/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void testCreatePlant() throws Exception {
        Mockito.when(service.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/plants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Basil"));
    }

    @Test
    void testUpdatePlant() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.update(eq(id), any())).thenReturn(Optional.of(dto));

        mockMvc.perform(put("/api/plants/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Basil"));
    }

    @Test
    void testUpdatePlantNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.update(eq(id), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/plants/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Plant not found"));
    }

    @Test
    void testDeletePlant() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/plants/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plant deleted successfully"));
    }

    @Test
    void testSearchPlants() throws Exception {
        Mockito.when(service.searchPlants(any(), any(), any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/search")
                        .param("query", "basil")
                        .param("gardenPlanId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testSearchPlantsAdvanced() throws Exception {
        Mockito.when(service.searchPlantsWithRelevance(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/search/advanced")
                        .param("name", "basil")
                        .param("type", "herb")
                        .param("wateringFrequency", "daily")
                        .param("sunlightRequirements", "full sun")
                        .param("query", "basil")
                        .param("gardenPlanId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testGetAllPlantsWithoutPagination() throws Exception {
        Mockito.when(service.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/plants/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Basil"));
    }

    @Test
    void testGetPlantsByGardenPlanId() throws Exception {
        UUID gardenPlanId = UUID.randomUUID();
        Mockito.when(service.findByGardenPlanId(eq(gardenPlanId), any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/garden-plan/" + gardenPlanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testGetPlantsByGardenPlanIdWithoutPagination() throws Exception {
        UUID gardenPlanId = UUID.randomUUID();
        Mockito.when(service.findByGardenPlanId(gardenPlanId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/plants/garden-plan/" + gardenPlanId + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Basil"));
    }

    @Test
    void testGetPlantsByType() throws Exception {
        Mockito.when(service.findByType(eq("Herb"), any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/type/Herb"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testGetPlantsByTypeWithoutPagination() throws Exception {
        Mockito.when(service.findByType("Herb")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/plants/type/Herb/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Basil"));
    }

    @Test
    void testErrorHandling() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.findById(id)).thenThrow(new RuntimeException("Test error"));

        mockMvc.perform(get("/api/plants/" + id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plant"));
    }
}
