package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.TestSecurityConfig;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantController.class)
@Import({PlantControllerTest.PlantTestConfig.class, TestSecurityConfig.class})
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
        Mockito.when(service.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

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
        Mockito.when(service.searchPlants(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/search")
                        .param("query", "basil")
                        .param("gardenPlanId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testSearchPlantsAdvanced() throws Exception {
        Mockito.when(service.searchPlantsWithRelevance(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
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
        Mockito.when(service.findByGardenPlanId(eq(gardenPlanId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

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
        Mockito.when(service.findByType(eq("Herb"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

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

    // New tests for better coverage

    @Test
    void testGetAllPlantsWithCustomPagination() throws Exception {
        Mockito.when(service.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "name")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testSearchPlantsWithCustomPagination() throws Exception {
        Mockito.when(service.searchPlants(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/search")
                        .param("query", "basil")
                        .param("gardenPlanId", UUID.randomUUID().toString())
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "name")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testSearchPlantsAdvancedWithCustomPagination() throws Exception {
        Mockito.when(service.searchPlantsWithRelevance(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/search/advanced")
                        .param("name", "basil")
                        .param("type", "herb")
                        .param("wateringFrequency", "daily")
                        .param("sunlightRequirements", "full sun")
                        .param("query", "basil")
                        .param("gardenPlanId", UUID.randomUUID().toString())
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testGetPlantsByGardenPlanIdWithCustomPagination() throws Exception {
        UUID gardenPlanId = UUID.randomUUID();
        Mockito.when(service.findByGardenPlanId(eq(gardenPlanId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/garden-plan/" + gardenPlanId)
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "name")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testGetPlantsByTypeWithCustomPagination() throws Exception {
        Mockito.when(service.findByType(eq("Herb"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/type/Herb")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sort", "name")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Basil"));
    }

    @Test
    void testCreatePlantWithInvalidData() throws Exception {
        PlantDTO invalidDto = new PlantDTO(
                UUID.randomUUID(),
                "", // Empty name
                "Herb",
                "desc",
                "Daily",
                "Full Sun",
                1,
                2,
                "url",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        mockMvc.perform(post("/api/plants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePlantWithInvalidData() throws Exception {
        UUID id = UUID.randomUUID();
        PlantDTO invalidDto = new PlantDTO(
                UUID.randomUUID(),
                "", // Empty name
                "Herb",
                "desc",
                "Daily",
                "Full Sun",
                1,
                2,
                "url",
                OffsetDateTime.now(),
                OffsetDateTime.now());

        mockMvc.perform(put("/api/plants/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchPlantsWithInvalidGardenPlanId() throws Exception {
        Mockito.when(service.searchPlants(any(), any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/search").param("query", "basil").param("gardenPlanId", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchPlantsAdvancedWithInvalidGardenPlanId() throws Exception {
        Mockito.when(service.searchPlantsWithRelevance(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/plants/search/advanced")
                        .param("name", "basil")
                        .param("type", "herb")
                        .param("wateringFrequency", "daily")
                        .param("sunlightRequirements", "full sun")
                        .param("query", "basil")
                        .param("gardenPlanId", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetPlantsByGardenPlanIdWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/plants/garden-plan/invalid-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void testGetPlantsByGardenPlanIdWithoutPaginationWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/plants/garden-plan/invalid-uuid/all")).andExpect(status().isBadRequest());
    }

    @Test
    void testGetPlantByIdWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/plants/invalid-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void testUpdatePlantWithInvalidId() throws Exception {
        mockMvc.perform(put("/api/plants/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeletePlantWithInvalidId() throws Exception {
        mockMvc.perform(delete("/api/plants/invalid-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void testCreatePlantWithServiceException() throws Exception {
        Mockito.when(service.create(any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/plants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to create plant"));
    }

    @Test
    void testUpdatePlantWithServiceException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(service.update(eq(id), any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/api/plants/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to update plant"));
    }

    @Test
    void testDeletePlantWithServiceException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new RuntimeException("Service error")).when(service).delete(id);

        mockMvc.perform(delete("/api/plants/" + id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to delete plant"));
    }

    @Test
    void testGetAllPlantsWithServiceException() throws Exception {
        Mockito.when(service.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants"));
    }

    @Test
    void testGetAllPlantsWithoutPaginationWithServiceException() throws Exception {
        Mockito.when(service.findAll()).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants"));
    }

    @Test
    void testSearchPlantsWithServiceException() throws Exception {
        Mockito.when(service.searchPlants(any(), any(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants/search")
                        .param("query", "basil")
                        .param("gardenPlanId", UUID.randomUUID().toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to search plants"));
    }

    @Test
    void testSearchPlantsAdvancedWithServiceException() throws Exception {
        Mockito.when(service.searchPlantsWithRelevance(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants/search/advanced")
                        .param("name", "basil")
                        .param("type", "herb")
                        .param("wateringFrequency", "daily")
                        .param("sunlightRequirements", "full sun")
                        .param("query", "basil")
                        .param("gardenPlanId", UUID.randomUUID().toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to perform advanced search on plants"));
    }

    @Test
    void testGetPlantsByGardenPlanIdWithServiceException() throws Exception {
        UUID gardenPlanId = UUID.randomUUID();
        Mockito.when(service.findByGardenPlanId(eq(gardenPlanId), any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants/garden-plan/" + gardenPlanId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants for garden plan"));
    }

    @Test
    void testGetPlantsByGardenPlanIdWithoutPaginationWithServiceException() throws Exception {
        UUID gardenPlanId = UUID.randomUUID();
        Mockito.when(service.findByGardenPlanId(gardenPlanId)).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants/garden-plan/" + gardenPlanId + "/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants for garden plan"));
    }

    @Test
    void testGetPlantsByTypeWithServiceException() throws Exception {
        Mockito.when(service.findByType(eq("Herb"), any(Pageable.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants/type/Herb"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants by type"));
    }

    @Test
    void testGetPlantsByTypeWithoutPaginationWithServiceException() throws Exception {
        Mockito.when(service.findByType("Herb")).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/plants/type/Herb/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to retrieve plants by type"));
    }
}
