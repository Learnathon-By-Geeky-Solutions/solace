package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.AddPlantDTO;
import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.service.AddPlantService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AddPlantController.class)
@Import({RateLimiterConfiguration.class, AddPlantControllerTest.AddPlantTestConfig.class})
class AddPlantControllerTest {

    @TestConfiguration
    static class AddPlantTestConfig {
        @Bean
        @Primary
        public AddPlantService addPlantService() {
            return org.mockito.Mockito.mock(AddPlantService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AddPlantService addPlantService;

    @Autowired
    private ObjectMapper objectMapper;

    private AddPlantDTO addPlantDTO;
    private PlantDTO plantDTO;
    private UUID gardenPlanId;
    private UUID plantsLibraryId;

    @BeforeEach
    void setUp() {
        gardenPlanId = UUID.randomUUID();
        plantsLibraryId = UUID.randomUUID();

        addPlantDTO = AddPlantDTO.builder()
                .gardenPlanId(gardenPlanId)
                .plantsLibraryId(plantsLibraryId)
                .build();

        plantDTO = PlantDTO.builder()
                .gardenPlanId(gardenPlanId)
                .name("Basil")
                .type("Herb")
                .description("A fragrant herb used in cooking")
                .wateringFrequency("Daily")
                .sunlightRequirements("Full Sun")
                .imageUrl("https://example.com/basil.jpg")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("addPlant should successfully add a plant from library")
    void addPlant_ShouldAddPlantFromLibrary() throws Exception {
        // Arrange
        when(addPlantService.addFromLibrary(any(AddPlantDTO.class))).thenReturn(plantDTO);

        // Act & Assert
        mockMvc.perform(post("/api/plants/from-library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addPlantDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plant added successfully"))
                .andExpect(jsonPath("$.data.name").value("Basil"))
                .andExpect(jsonPath("$.data.type").value("Herb"))
                .andExpect(jsonPath("$.data.description").value("A fragrant herb used in cooking"))
                .andExpect(jsonPath("$.data.wateringFrequency").value("Daily"))
                .andExpect(jsonPath("$.data.sunlightRequirements").value("Full Sun"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/basil.jpg"));
    }

    @Test
    @DisplayName("addPlant should return bad request when gardenPlanId is missing")
    void addPlant_ShouldReturnBadRequest_WhenGardenPlanIdIsMissing() throws Exception {
        // Arrange
        AddPlantDTO invalidDTO =
                AddPlantDTO.builder().plantsLibraryId(plantsLibraryId).build();

        // Act & Assert
        mockMvc.perform(post("/api/plants/from-library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("addPlant should return bad request when plantsLibraryId is missing")
    void addPlant_ShouldReturnBadRequest_WhenPlantsLibraryIdIsMissing() throws Exception {
        // Arrange
        AddPlantDTO invalidDTO =
                AddPlantDTO.builder().gardenPlanId(gardenPlanId).build();

        // Act & Assert
        mockMvc.perform(post("/api/plants/from-library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("addPlant should return internal server error when service throws exception")
    void addPlant_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(addPlantService.addFromLibrary(any(AddPlantDTO.class))).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/plants/from-library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addPlantDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to add plant"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
    }
}
