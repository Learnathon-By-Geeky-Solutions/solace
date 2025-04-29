package dev.solace.twiggle.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.solace.twiggle.dto.PlantDiseaseDTO;
import dev.solace.twiggle.service.PlantDiseaseService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(PlantDiseaseController.class)
@Import(PlantDiseaseControllerTest.PlantDiseaseTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class PlantDiseaseControllerTest {

    @TestConfiguration
    static class PlantDiseaseTestConfig {
        @Bean
        @Primary
        public PlantDiseaseService plantDiseaseService() {
            return mock(PlantDiseaseService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlantDiseaseService plantDiseaseService;

    private PlantDiseaseDTO sampleDisease;

    @BeforeEach
    void setUp() {
        sampleDisease = PlantDiseaseDTO.builder()
                .id(1L)
                .commonName("Powdery Mildew")
                .scientificName("Erysiphe spp.")
                .description("A fungal disease characterized by white powdery growth on leaves and stems.")
                .symptoms("White or gray powdery spots on leaves; distorted growth; premature leaf drop.")
                .favorableConditions("Warm days and cool nights; high humidity; poor air circulation.")
                .preventionTips("Ensure good air circulation; avoid overhead watering; plant resistant varieties.")
                .organicControl("Apply sulfur sprays or neem oil at first sign of disease.")
                .chemicalControl("Use fungicides containing myclobutanil or propiconazole as needed.")
                .imageUrl("https://example.com/powdery-mildew.jpg")
                .transmissionMethod("Airborne spores")
                .contagiousness("High")
                .severityRating("Moderate")
                .timeToOnset("3-7 days after infection")
                .recoveryChances("High with timely treatment")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void testGetAllDiseases() throws Exception {
        when(plantDiseaseService.findAll()).thenReturn(List.of(sampleDisease));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-diseases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully retrieved all diseases"))
                .andExpect(jsonPath("$.data[0].commonName").value("Powdery Mildew"))
                .andExpect(jsonPath("$.data[0].scientificName").value("Erysiphe spp."));
    }

    @Test
    void testGetDiseasesByPlantLibraryId() throws Exception {
        UUID plantLibraryId = UUID.randomUUID();
        when(plantDiseaseService.findByPlantLibraryId(plantLibraryId)).thenReturn(List.of(sampleDisease));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/plant-diseases/plant-library/" + plantLibraryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Successfully retrieved diseases for plant library ID: " + plantLibraryId))
                .andExpect(jsonPath("$.data[0].symptoms")
                        .value("White or gray powdery spots on leaves; distorted growth; premature leaf drop."));
    }
}
