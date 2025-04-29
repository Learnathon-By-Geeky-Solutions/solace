package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import dev.solace.twiggle.service.client.OpenAiClient;
import dev.solace.twiggle.service.client.UnsplashClient;
import dev.solace.twiggle.service.util.JsonUtils;
import dev.solace.twiggle.service.util.SeasonalUtils;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlantRecommendationServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    @Mock
    private UnsplashClient unsplashClient;

    @Mock
    private JsonUtils jsonUtils;

    @Mock
    private SeasonalUtils seasonalUtils;

    @Mock
    private PromptBuilder promptBuilder;

    private PlantRecommendationService plantRecommendationService;
    private PlantRecommendationRequest validRequest;
    private List<PlantRecommendation> mockRecommendations;
    private List<PlantRecommendation> mockRecommendationsWithImages;

    @BeforeEach
    void setUp() {
        plantRecommendationService =
                new PlantRecommendationService(openAiClient, unsplashClient, jsonUtils, seasonalUtils, promptBuilder);

        validRequest = PlantRecommendationRequest.builder()
                .location("San Francisco")
                .gardenType("balcony")
                .message("Easy plants for beginners")
                .userPreferences(PlantRecommendationRequest.UserPreferences.builder()
                        .experience("beginner")
                        .timeCommitment("moderate")
                        .harvestGoals(new ArrayList<>())
                        .build())
                .build();

        mockRecommendations = List.of(
                PlantRecommendation.builder()
                        .name("Tomato")
                        .type("Vegetable")
                        .description("Easy to grow")
                        .build(),
                PlantRecommendation.builder()
                        .name("Basil")
                        .type("Herb")
                        .description("Companion plant")
                        .build());

        mockRecommendationsWithImages = new ArrayList<>();
        for (PlantRecommendation rec : mockRecommendations) {
            mockRecommendationsWithImages.add(PlantRecommendation.builder()
                    .name(rec.getName())
                    .type(rec.getType())
                    .description(rec.getDescription())
                    .imageURL("https://example.com/image.jpg")
                    .build());
        }
    }

    @Test
    void getPlantRecommendations_WithValidRequest_ShouldReturnRecommendations() throws JsonProcessingException {
        // Arrange
        when(seasonalUtils.getCurrentSeason("San Francisco")).thenReturn("summer");
        when(promptBuilder.buildSystemPrompt("summer")).thenReturn("System prompt");
        when(promptBuilder.buildUserPrompt(validRequest, "summer")).thenReturn("User prompt");
        when(openAiClient.fetchRecommendations("System prompt", "User prompt")).thenReturn("OpenAI response");
        when(jsonUtils.extractRecommendationsFromOpenAiResponse("OpenAI response"))
                .thenReturn(mockRecommendations);

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNull(result.getError());
        assertEquals(mockRecommendations.size(), result.getRecommendations().size());
        assertEquals(
                mockRecommendations.getFirst().getName(),
                result.getRecommendations().getFirst().getName());

        // Verify interactions
        verify(seasonalUtils).getCurrentSeason("San Francisco");
        verify(promptBuilder).buildSystemPrompt("summer");
        verify(promptBuilder).buildUserPrompt(validRequest, "summer");
        verify(openAiClient).fetchRecommendations("System prompt", "User prompt");
        verify(jsonUtils).extractRecommendationsFromOpenAiResponse("OpenAI response");
        verify(unsplashClient, times(mockRecommendations.size())).fetchPlantImage(any(PlantRecommendation.class));
    }

    @Test
    void getPlantRecommendations_WhenOpenAiFails_ShouldReturnErrorResponse() throws JsonProcessingException {
        // Arrange
        when(seasonalUtils.getCurrentSeason("San Francisco")).thenReturn("summer");
        when(promptBuilder.buildSystemPrompt("summer")).thenReturn("System prompt");
        when(promptBuilder.buildUserPrompt(validRequest, "summer")).thenReturn("User prompt");
        when(openAiClient.fetchRecommendations("System prompt", "User prompt"))
                .thenThrow(mock(WebClientResponseException.class));

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("OpenAI API"));
        assertNull(result.getRecommendations());
    }

    @Test
    void getPlantRecommendations_WhenJsonParsingFails_ShouldReturnErrorResponse() throws JsonProcessingException {
        // Arrange
        when(seasonalUtils.getCurrentSeason("San Francisco")).thenReturn("summer");
        when(promptBuilder.buildSystemPrompt("summer")).thenReturn("System prompt");
        when(promptBuilder.buildUserPrompt(validRequest, "summer")).thenReturn("User prompt");
        when(openAiClient.fetchRecommendations("System prompt", "User prompt")).thenReturn("OpenAI response");
        when(jsonUtils.extractRecommendationsFromOpenAiResponse("OpenAI response"))
                .thenThrow(new JsonProcessingException("JSON parsing failed") {});

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("JSON parsing failed"));
        assertNull(result.getRecommendations());
    }

    @Test
    void getPlantRecommendations_WithNullUserPreferences_ShouldSetDefaults() throws JsonProcessingException {
        // Arrange
        PlantRecommendationRequest requestWithNullPrefs = PlantRecommendationRequest.builder()
                .location("San Francisco")
                .gardenType("balcony")
                .message("Easy plants for beginners")
                .userPreferences(null)
                .build();

        when(seasonalUtils.getCurrentSeason("San Francisco")).thenReturn("summer");
        when(promptBuilder.buildSystemPrompt("summer")).thenReturn("System prompt");
        when(promptBuilder.buildUserPrompt(any(), eq("summer"))).thenReturn("User prompt");
        when(openAiClient.fetchRecommendations("System prompt", "User prompt")).thenReturn("OpenAI response");
        when(jsonUtils.extractRecommendationsFromOpenAiResponse("OpenAI response"))
                .thenReturn(mockRecommendations);

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(requestWithNullPrefs);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(requestWithNullPrefs.getUserPreferences());
        assertEquals("beginner", requestWithNullPrefs.getUserPreferences().getExperience());
        assertEquals("moderate", requestWithNullPrefs.getUserPreferences().getTimeCommitment());
        assertTrue(requestWithNullPrefs.getUserPreferences().getHarvestGoals().isEmpty());
    }

    @Test
    void buildSuccessResponse_ShouldCreateCorrectResponse() throws JsonProcessingException {
        // Arrange
        String season = "summer";

        // Act
        PlantRecommendationResponse result = plantRecommendationService.getPlantRecommendations(validRequest);

        // Assert, checking only the meta fields since we've mocked the response
        assertNotNull(result.getMeta());
        assertEquals(validRequest.getGardenType(), result.getMeta().getGardenType());
    }

    @Test
    void buildSuccessResponse_WithNullLocation_ShouldHandleGracefully() throws JsonProcessingException {
        // Arrange
        PlantRecommendationRequest requestWithNullLocation = PlantRecommendationRequest.builder()
                .gardenType("balcony")
                .message("test")
                .build();

        when(seasonalUtils.getCurrentSeason(null)).thenReturn("summer");
        when(promptBuilder.buildSystemPrompt("summer")).thenReturn("System prompt");
        when(promptBuilder.buildUserPrompt(any(), eq("summer"))).thenReturn("User prompt");
        when(openAiClient.fetchRecommendations("System prompt", "User prompt")).thenReturn("OpenAI response");
        when(jsonUtils.extractRecommendationsFromOpenAiResponse(anyString())).thenReturn(mockRecommendations);

        // Act
        PlantRecommendationResponse result =
                plantRecommendationService.getPlantRecommendations(requestWithNullLocation);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Unknown", result.getMeta().getLocation());
    }
}
