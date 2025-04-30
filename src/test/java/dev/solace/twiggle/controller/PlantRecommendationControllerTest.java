package dev.solace.twiggle.controller;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.config.RateLimiterConfiguration;
import dev.solace.twiggle.config.TestSecurityConfig;
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
@Import({
    RateLimiterConfiguration.class,
    PlantRecommendationControllerTest.PlantRecommendationTestConfig.class,
    TestSecurityConfig.class
})
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

    @Test
    void getPlantRecommendations_ReturnsErrorResponseWhenServiceReturnsErrorResponse() throws Exception {
        // Arrange
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("Need easy plants")
                .build();

        // Service returns an unsuccessful response with an error message
        PlantRecommendationResponse errorResponse = PlantRecommendationResponse.builder()
                .success(false)
                .error("Failed to generate plant recommendations due to API limitations")
                .build();

        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.message").value("Failed to generate plant recommendations due to API limitations"));
    }

    @Test
    void getPlantRecommendations_WithNullRecommendationsArray_HandlesCountCorrectly() throws Exception {
        // Arrange
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("Need easy plants")
                .build();

        // Service returns a successful response but with null recommendations
        PlantRecommendationResponse response = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(null) // Explicitly null to test the null check
                .meta(PlantRecommendationResponse.MetaData.builder()
                        .location("Test City")
                        .gardenType("balcony")
                        .season("Spring")
                        .build())
                .build();

        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plant recommendations retrieved successfully"))
                .andExpect(jsonPath("$.data.recommendations").doesNotExist());
    }

    @Test
    void getPlantRecommendations_WithEmptyRecommendationsArray_LogsCorrectCount() throws Exception {
        // Arrange
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("Need easy plants")
                .build();

        // Service returns a successful response but with empty recommendations list
        PlantRecommendationResponse response = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(List.of()) // Empty list
                .meta(PlantRecommendationResponse.MetaData.builder()
                        .location("Test City")
                        .gardenType("balcony")
                        .season("Spring")
                        .build())
                .build();

        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plant recommendations retrieved successfully"))
                .andExpect(jsonPath("$.data.recommendations").isArray())
                .andExpect(jsonPath("$.data.recommendations").isEmpty());
    }

    @Test
    void getPlantRecommendations_WithCompleteUserPreferences_ShouldWorkCorrectly() throws Exception {
        // Arrange
        PlantRecommendationRequest.UserPreferences preferences = PlantRecommendationRequest.UserPreferences.builder()
                .experience("expert")
                .timeCommitment("high")
                .harvestGoals(List.of("vegetables", "herbs"))
                .build();

        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("Need easy plants")
                .userPreferences(preferences)
                .existingPlants(List.of("Tomato", "Basil"))
                .build();

        List<PlantRecommendation> mockRecommendations = List.of(PlantRecommendation.builder()
                .name("Cucumber")
                .description("Good companion")
                .build());

        PlantRecommendationResponse mockResponse = PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(mockRecommendations)
                .meta(PlantRecommendationResponse.MetaData.builder()
                        .location("Test City")
                        .gardenType("balcony")
                        .season("Spring")
                        .build())
                .build();

        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Plant recommendations retrieved successfully"))
                .andExpect(jsonPath("$.data.recommendations[0].name").value("Cucumber"));
    }

    // Replace the failing test with these three alternative tests

    @Test
    void testDefaultsUsingDifferentApproach() throws Exception {
        // Instead of testing with empty fields (which triggers validation),
        // let's test by omitting optional fields like message and verifying defaults

        // Create request with required fields but omit optional ones
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                // No message, existingPlants, or userPreferences
                .build();

        List<PlantRecommendation> mockRecommendations = List.of(PlantRecommendation.builder()
                .name("Default Plant")
                .type("Flower")
                .description("Easy")
                .build());

        // Use thenAnswer to inspect the actual request sent to the service
        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenAnswer(invocation -> {
                    PlantRecommendationRequest actualRequest = invocation.getArgument(0);

                    // Verify defaults were applied to optional fields
                    assertEquals("Recommend plants", actualRequest.getMessage());
                    assertNotNull(actualRequest.getExistingPlants());
                    assertTrue(actualRequest.getExistingPlants().isEmpty());
                    assertNotNull(actualRequest.getUserPreferences());
                    assertEquals("beginner", actualRequest.getUserPreferences().getExperience());
                    assertEquals("moderate", actualRequest.getUserPreferences().getTimeCommitment());

                    return PlantRecommendationResponse.builder()
                            .success(true)
                            .recommendations(mockRecommendations)
                            .meta(PlantRecommendationResponse.MetaData.builder()
                                    .location("Test City")
                                    .gardenType("balcony")
                                    .season("Spring")
                                    .build())
                            .build();
                });

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations[0].name").value("Default Plant"));
    }

    @Test
    void testDefaultValueAppliedToNonValidatedField() throws Exception {
        // For this test, let's focus on the "message" field which doesn't have @NotBlank
        // but will still be defaulted if blank

        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("") // Empty string should get defaulted
                .build();

        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenAnswer(invocation -> {
                    PlantRecommendationRequest actualRequest = invocation.getArgument(0);

                    // Verify default was applied to message
                    assertEquals("Recommend plants", actualRequest.getMessage());

                    return PlantRecommendationResponse.builder()
                            .success(true)
                            .recommendations(List.of(PlantRecommendation.builder()
                                    .name("Test Plant")
                                    .type("Test")
                                    .build()))
                            .build();
                });

        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testApplyDefaultsIfNeeded() {
        // This is a direct unit test of the DTO method, not via controller

        // 1. Test with empty fields
        PlantRecommendationRequest request1 = PlantRecommendationRequest.builder()
                .location("Required") // Can't be empty due to @NotBlank
                .gardenType("Required") // Can't be empty due to @NotBlank
                .message("")
                .build();

        request1.applyDefaultsIfNeeded();

        assertEquals("Recommend plants", request1.getMessage());
        assertNotNull(request1.getExistingPlants());
        assertNotNull(request1.getUserPreferences());

        // 2. Test with null fields
        PlantRecommendationRequest request2 = PlantRecommendationRequest.builder()
                .location("Required")
                .gardenType("Required")
                .build();

        request2.applyDefaultsIfNeeded();

        assertEquals("Recommend plants", request2.getMessage());
        assertNotNull(request2.getExistingPlants());
        assertNotNull(request2.getUserPreferences());
        assertEquals("beginner", request2.getUserPreferences().getExperience());

        // 3. Test when UserPreferences exists but has empty/null fields
        PlantRecommendationRequest.UserPreferences incompletePrefs =
                PlantRecommendationRequest.UserPreferences.builder()
                        .experience("")
                        .build();

        PlantRecommendationRequest request3 = PlantRecommendationRequest.builder()
                .location("Required")
                .gardenType("Required")
                .userPreferences(incompletePrefs)
                .build();

        request3.applyDefaultsIfNeeded();

        assertEquals("beginner", request3.getUserPreferences().getExperience());
        assertEquals("moderate", request3.getUserPreferences().getTimeCommitment());
        assertNotNull(request3.getUserPreferences().getHarvestGoals());
    }

    @Test
    void getPlantRecommendations_WithMissingUserPreferences_ShouldApplyDefaults() throws Exception {
        // Arrange - Request without user preferences to test defaults being applied
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .message("Need easy plants")
                // No userPreferences
                .build();

        // Need to capture the actual request that gets passed to the service to verify defaults
        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenAnswer(invocation -> {
                    PlantRecommendationRequest actualRequest = invocation.getArgument(0);

                    // Verify defaults were applied
                    assertNotNull(actualRequest.getUserPreferences());
                    assertEquals("beginner", actualRequest.getUserPreferences().getExperience());
                    assertEquals("moderate", actualRequest.getUserPreferences().getTimeCommitment());
                    assertNotNull(actualRequest.getUserPreferences().getHarvestGoals());
                    assertTrue(
                            actualRequest.getUserPreferences().getHarvestGoals().isEmpty());

                    // Return a success response
                    return PlantRecommendationResponse.builder()
                            .success(true)
                            .recommendations(List.of(PlantRecommendation.builder()
                                    .name("Default Plant")
                                    .build()))
                            .build();
                });

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getPlantRecommendations_WithIncompleteUserPreferences_ShouldApplyDefaults() throws Exception {
        // Arrange - Request with incomplete user preferences
        PlantRecommendationRequest.UserPreferences incompletePrefs =
                PlantRecommendationRequest.UserPreferences.builder()
                        .experience("") // Empty, should be defaulted
                        // Missing timeCommitment
                        // Missing harvestGoals
                        .build();

        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("Test City")
                .gardenType("balcony")
                .userPreferences(incompletePrefs)
                .build();

        // Capture and verify
        when(plantRecommendationService.getPlantRecommendations(any(PlantRecommendationRequest.class)))
                .thenAnswer(invocation -> {
                    PlantRecommendationRequest actualRequest = invocation.getArgument(0);

                    // Verify defaults were applied
                    assertNotNull(actualRequest.getUserPreferences());
                    assertEquals("beginner", actualRequest.getUserPreferences().getExperience());
                    assertEquals("moderate", actualRequest.getUserPreferences().getTimeCommitment());
                    assertNotNull(actualRequest.getUserPreferences().getHarvestGoals());
                    assertTrue(
                            actualRequest.getUserPreferences().getHarvestGoals().isEmpty());

                    return PlantRecommendationResponse.builder()
                            .success(true)
                            .recommendations(List.of(PlantRecommendation.builder()
                                    .name("Default Plant")
                                    .build()))
                            .build();
                });

        // Act & Assert
        mockMvc.perform(post("/api/v1/plant-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
