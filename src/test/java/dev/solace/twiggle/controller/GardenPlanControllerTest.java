package dev.solace.twiggle.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.GardenPlanDTO;
import dev.solace.twiggle.service.GardenPlanService;
import java.time.OffsetDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

@WebMvcTest(GardenPlanController.class)
@Import(GardenPlanControllerTest.GardenPlanTestConfig.class)
class GardenPlanControllerTest {

    @TestConfiguration
    static class GardenPlanTestConfig {
        @Bean
        @Primary
        public GardenPlanService gardenPlanService() {
            return Mockito.mock(GardenPlanService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GardenPlanService gardenPlanService;

    private GardenPlanDTO dto;

    @BeforeEach
    void setUp() {
        // clear any stubbing from previous tests
        reset(gardenPlanService);

        dto = GardenPlanDTO.builder()
                .userId(UUID.randomUUID())
                .name("Urban Garden")
                .type("Balcony")
                .description("Rooftop garden setup")
                .location("Dhaka")
                .thumbnailUrl("https://example.com/image.jpg")
                .isPublic(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    /* ----------  Happy-path endpoints  ---------- */

    @Test
    void testGetAllGardenPlans() throws Exception {
        Mockito.when(gardenPlanService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Urban Garden"));
    }

    @Test
    void testGetAllGardenPlansWithoutPagination() throws Exception {
        Mockito.when(gardenPlanService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/garden-plans/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("Balcony"));
    }

    @Test
    void testGetGardenPlanById_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenPlanService.findById(id)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/garden-plans/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Urban Garden"));
    }

    @Test
    void testGetGardenPlanById_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenPlanService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/garden-plans/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void testCreateGardenPlan() throws Exception {
        Mockito.when(gardenPlanService.create(any())).thenReturn(dto);

        mockMvc.perform(post("/api/garden-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden plan created successfully"));
    }

    @Test
    void testUpdateGardenPlan_found() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenPlanService.update(Mockito.eq(id), any())).thenReturn(Optional.of(dto));

        mockMvc.perform(put("/api/garden-plans/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Urban Garden"));
    }

    @Test
    void testUpdateGardenPlan_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenPlanService.update(Mockito.eq(id), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/garden-plans/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteGardenPlan() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/garden-plans/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Garden plan deleted successfully"));
    }

    @Test
    void testSearchGardenPlans() throws Exception {
        Mockito.when(gardenPlanService.searchGardenPlans(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans/search").param("query", "garden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testAdvancedSearchGardenPlans() throws Exception {
        Mockito.when(gardenPlanService.searchGardenPlansWithRelevance(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans/search/advanced")
                        .param("name", "Urban")
                        .param("type", "Balcony"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].location").value("Dhaka"));
    }

    /* ----------  Pagination / sorting helpers  ---------- */

    @Test
    void testGetGardenPlansByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(gardenPlanService.findByUserId(Mockito.eq(userId), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Urban Garden"));
    }

    @Test
    void testGetGardenPlansByUserIdWithoutPagination() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(gardenPlanService.findByUserId(userId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/garden-plans/user/{userId}/all", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("Balcony"));
    }

    @Test
    void testGetPublicGardenPlans() throws Exception {
        Mockito.when(gardenPlanService.findPublicPlans(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans/public")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Urban Garden"));
    }

    @Test
    void testGetPublicGardenPlansWithoutPagination() throws Exception {
        Mockito.when(gardenPlanService.findPublicPlans()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/garden-plans/public/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("Balcony"));
    }

    /* ----------  Validation & error-path tests  ---------- */

    @Test
    void testGetAllGardenPlansWithInvalidSortDirection() throws Exception {
        Mockito.when(gardenPlanService.findAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans").param("direction", "INVALID"))
                .andExpect(status().isInternalServerError());
    }

    @ParameterizedTest
    @CsvSource({"direction, INVALID", "page, -1", "size, 0"})
    void testGetAllGardenPlansWithInvalidParameters(String param, String value) throws Exception {
        Mockito.when(gardenPlanService.findAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans").param(param, value)).andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateGardenPlanWithInvalidData() throws Exception {
        GardenPlanDTO invalidDto = GardenPlanDTO.builder().name("").build(); // empty name

        mockMvc.perform(post("/api/garden-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Validation failed. Please check the provided data.")));
    }

    @Test
    void testCreateGardenPlanWithServiceException() throws Exception {
        Mockito.when(gardenPlanService.create(any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/garden-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateGardenPlanWithServiceException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenPlanService.update(Mockito.eq(id), any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/api/garden-plans/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteGardenPlanWithServiceException() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doThrow(new RuntimeException("Service error"))
                .when(gardenPlanService)
                .delete(id);

        mockMvc.perform(delete("/api/garden-plans/{id}", id)).andExpect(status().isInternalServerError());
    }

    @Test
    void testSearchGardenPlansWithInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/garden-plans/search").param("userId", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSearchGardenPlansWithEmptyResults() throws Exception {
        Mockito.when(gardenPlanService.searchGardenPlans(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/garden-plans/search").param("query", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    void testAdvancedSearchGardenPlansWithInvalidBoolean() throws Exception {
        mockMvc.perform(get("/api/garden-plans/search/advanced").param("isPublic", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetGardenPlansByUserIdWithInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/garden-plans/user/{userId}", "invalid-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void testGetGardenPlansByUserIdWithServiceException() throws Exception {
        UUID userId = UUID.randomUUID();
        Mockito.when(gardenPlanService.findByUserId(Mockito.eq(userId), any()))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/garden-plans/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetPublicGardenPlansWithServiceException() throws Exception {
        Mockito.when(gardenPlanService.findPublicPlans(any())).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/garden-plans/public").param("page", "0").param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSearchGardenPlansWithNullParameters() throws Exception {
        Mockito.when(gardenPlanService.searchGardenPlans(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans/search")
                        .param("query", "")
                        .param("userId", "")
                        .param("isPublic", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testAdvancedSearchGardenPlansWithNullParameters() throws Exception {
        Mockito.when(gardenPlanService.searchGardenPlansWithRelevance(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/garden-plans/search/advanced")
                        .param("name", "")
                        .param("type", "")
                        .param("location", "")
                        .param("query", "")
                        .param("userId", "")
                        .param("isPublic", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
