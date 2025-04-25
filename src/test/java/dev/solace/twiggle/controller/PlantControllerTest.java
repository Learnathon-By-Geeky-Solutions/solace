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
    void testDeletePlant() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/plants/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plant deleted successfully"));
    }
}
