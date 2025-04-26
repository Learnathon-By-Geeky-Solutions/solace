package dev.solace.twiggle.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import dev.solace.twiggle.service.PlantRecommendationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantRecommendationController.class)
@Import({RateLimiterConfiguration.class, PlantRecommendationControllerTest.PlantRecommendationTestConfig.class})
class PlantRecommendationControllerTest {

    @TestConfiguration
    static class PlantRecommendationTestConfig {
        @Bean
        @Primary
        public PlantRecommendationService plantRecommendationService() {
            return org.mockito.Mockito.mock(PlantRecommendationService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlantRecommendationService plantRecommendationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPlantRecommendations_WithValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("Need easy plants")
                .build();

        List<PlantRecommendation> mockRecommendations = List.of(
                PlantRecommendation.builder().name("Tomato").description("Easy").build(),
                PlantRecommendation.builder()
                        .name("Basil")
                        .description("Goes with tomato")
                        .build());

        PlantRecommendationResponse mockResponse = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(mockRecommendations)
                .meta(PlantRecommendationResponse.MetaData.builder()
                        .location("Test City")
                        .gardenType("balcony")
                        .season("Spring") // Assume a season for the test
                        .build())
                .build();

        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Send request body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plant recommendations retrieved successfully"))
                .andExpect(jsonPath("$.data.recommendations[0].name").value("Tomato"))
                .andExpect(jsonPath("$.data.recommendations[1].name").value("Basil"))
                .andExpect(jsonPath("$.data.meta.location").value("Test City"))
                .andExpect(jsonPath("$.data.meta.gardenType").value("balcony"));
    }

    @Test
    void getPlantRecommendations_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange - Sending an empty request body or one missing required fields
        PlantRecommendationRequest invalidRequest =
                PlantRecommendationRequest.builder().build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Expecting validation to fail
    }

    @Test
    void getPlantRecommendations_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("Need easy plants")
                .build();

        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // Removed tests for getCompanionPlants as the method doesn't exist in the
    // service anymore
}
