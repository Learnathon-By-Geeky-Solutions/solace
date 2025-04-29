package dev.solace.twiggle.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromptBuilderTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder();
    }

    @Test
    void buildSystemPrompt_ShouldIncludeSeason() {
        // Act
        String prompt = promptBuilder.buildSystemPrompt("spring");

        // Assert
        assertTrue(prompt.contains("spring"));
        assertTrue(prompt.contains("seasonal_tips"));
        assertTrue(prompt.contains("valid, parseable JSON"));
    }

    @Test
    void buildUserPrompt_ShouldIncludeAllContextFields() {
        // Arrange
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .location("London")
                .gardenType("indoor")
                .message("Need low-maintenance plants")
                .userPreferences(PlantRecommendationRequest.UserPreferences.builder()
                        .experience("expert")
                        .timeCommitment("low")
                        .harvestGoals(List.of("herbs", "vegetables"))
                        .build())
                .existingPlants(List.of("monstera", "pothos"))
                .build();

        // Act
        String prompt = promptBuilder.buildUserPrompt(request, "spring");

        // Assert
        assertTrue(prompt.contains("London"));
        assertTrue(prompt.contains("indoor"));
        assertTrue(prompt.contains("expert"));
        assertTrue(prompt.contains("low"));
        assertTrue(prompt.contains("herbs, vegetables"));
        assertTrue(prompt.contains("monstera, pothos"));
        assertTrue(prompt.contains("Need low-maintenance plants"));
        assertTrue(prompt.contains("spring"));
    }

    @Test
    void buildUserPrompt_ShouldHandleNullValues() {
        // Arrange
        PlantRecommendationRequest request = PlantRecommendationRequest.builder()
                .gardenType("balcony")
                .message("What should I plant?")
                .userPreferences(
                        PlantRecommendationRequest.UserPreferences.builder().build())
                .build();

        // Act
        String prompt = promptBuilder.buildUserPrompt(request, "winter");

        // Assert
        assertTrue(prompt.contains("Unknown"));
        assertTrue(prompt.contains("balcony"));
        assertTrue(prompt.contains("winter"));
        assertTrue(prompt.contains("beginner"));
        assertTrue(prompt.contains("moderate"));
        assertTrue(prompt.contains("general gardening"));
        assertTrue(prompt.contains("None yet"));
        assertTrue(prompt.contains("What should I plant?"));
    }

    @Test
    void formatHarvestGoals_ShouldHandleNullAndEmptyLists() {
        // Arrange
        PlantRecommendationRequest.UserPreferences emptyPrefs = PlantRecommendationRequest.UserPreferences.builder()
                .harvestGoals(List.of())
                .build();

        PlantRecommendationRequest.UserPreferences nullPrefs = PlantRecommendationRequest.UserPreferences.builder()
                .harvestGoals(null)
                .build();

        PlantRecommendationRequest.UserPreferences filledPrefs = PlantRecommendationRequest.UserPreferences.builder()
                .harvestGoals(List.of("fruits", "vegetables"))
                .build();

        // Create a request to test the private method through the public one
        PlantRecommendationRequest emptyRequest =
                PlantRecommendationRequest.builder().userPreferences(emptyPrefs).build();

        PlantRecommendationRequest nullRequest =
                PlantRecommendationRequest.builder().userPreferences(nullPrefs).build();

        PlantRecommendationRequest filledRequest = PlantRecommendationRequest.builder()
                .userPreferences(filledPrefs)
                .build();

        // Act and Assert through the public method
        String emptyResult = promptBuilder.buildUserPrompt(emptyRequest, "winter");
        assertTrue(emptyResult.contains("general gardening"));

        String nullResult = promptBuilder.buildUserPrompt(nullRequest, "winter");
        assertTrue(nullResult.contains("general gardening"));

        String filledResult = promptBuilder.buildUserPrompt(filledRequest, "winter");
        assertTrue(filledResult.contains("fruits, vegetables"));
    }
}
