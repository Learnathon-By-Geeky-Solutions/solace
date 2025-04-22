package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.GardenPlanDTO;
import dev.solace.twiggle.service.GardenPlanService;
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

@WebMvcTest(GardenPlanController.class)
class GardenPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GardenPlanService gardenPlanService;

    private GardenPlanDTO dto;

    @BeforeEach
    void setUp() {
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

    @Test
    void testGetAllGardenPlans() throws Exception {
        Page<GardenPlanDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenPlanService.findAll(any())).thenReturn(page);

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
        Mockito.when(gardenPlanService.update(eq(id), any())).thenReturn(Optional.of(dto));

        mockMvc.perform(put("/api/garden-plans/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Urban Garden"));
    }

    @Test
    void testUpdateGardenPlan_notFound() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(gardenPlanService.update(eq(id), any())).thenReturn(Optional.empty());

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
        Page<GardenPlanDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenPlanService.searchGardenPlans(any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/garden-plans/search?query=garden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void testAdvancedSearchGardenPlans() throws Exception {
        Page<GardenPlanDTO> page = new PageImpl<>(List.of(dto));
        Mockito.when(gardenPlanService.searchGardenPlansWithRelevance(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/garden-plans/search/advanced?name=Urban&type=Balcony"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].location").value("Dhaka"));
    }
}
