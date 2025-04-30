package dev.solace.twiggle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import dev.solace.twiggle.service.client.OpenAiClient;
import dev.solace.twiggle.service.client.UnsplashClient;
import dev.solace.twiggle.service.util.JsonUtils;
import dev.solace.twiggle.service.util.SeasonalUtils;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class PlantRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(PlantRecommendationService.class);
    private static final String UNKNOWN = "Unknown";

    private final OpenAiClient openAiClient;
    private final UnsplashClient unsplashClient;
    private final JsonUtils jsonUtils;
    private final SeasonalUtils seasonalUtils;
    private final PromptBuilder promptBuilder;

    public PlantRecommendationService(
            OpenAiClient openAiClient,
            UnsplashClient unsplashClient,
            JsonUtils jsonUtils,
            SeasonalUtils seasonalUtils,
            PromptBuilder promptBuilder) {
        this.openAiClient = openAiClient;
        this.unsplashClient = unsplashClient;
        this.jsonUtils = jsonUtils;
        this.seasonalUtils = seasonalUtils;
        this.promptBuilder = promptBuilder;
    }

    public PlantRecommendationResponse getPlantRecommendations(PlantRecommendationRequest request) {
        logRequestDetails(request);
        setDefaultUserPreferencesIfAbsent(request);

        String currentSeason = seasonalUtils.getCurrentSeason(request.getLocation());
        log.info("Current season: {}", currentSeason);

        try {
            String openAiResponse = openAiClient.fetchRecommendations(
                    promptBuilder.buildSystemPrompt(currentSeason),
                    promptBuilder.buildUserPrompt(request, currentSeason));

            List<PlantRecommendation> recommendations = parseRecommendationsFromJson(openAiResponse);
            recommendations.forEach(unsplashClient::fetchPlantImage);

            return buildSuccessResponse(request, currentSeason, recommendations);
        } catch (WebClientResponseException e) {
            return handleWebClientError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }

    private void logRequestDetails(PlantRecommendationRequest request) {
        log.info("Getting plant recommendations for {} garden", request.getGardenType());
        log.info("Location: {}", request.getLocation() != null ? request.getLocation() : UNKNOWN);
        log.info(
                "Existing plants: {}",
                request.getExistingPlants() != null
                                && !request.getExistingPlants().isEmpty()
                        ? String.join(", ", request.getExistingPlants())
                        : "None");
        log.info("User message: {}", request.getMessage());
    }

    private void setDefaultUserPreferencesIfAbsent(PlantRecommendationRequest request) {
        if (request.getUserPreferences() == null) {
            request.setUserPreferences(PlantRecommendationRequest.UserPreferences.builder()
                    .experience("beginner")
                    .harvestGoals(new ArrayList<>())
                    .timeCommitment("moderate")
                    .build());
        }
    }

    private List<PlantRecommendation> parseRecommendationsFromJson(String openAiResponse) {
        try {
            return jsonUtils.extractRecommendationsFromOpenAiResponse(openAiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to parse plant recommendations from OpenAI response: " + e.getMessage(), e);
        }
    }

    private PlantRecommendationResponse buildSuccessResponse(
            PlantRecommendationRequest request, String season, List<PlantRecommendation> recommendations) {
        return PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(recommendations)
                .meta(PlantRecommendationResponse.MetaData.builder()
                        .season(season)
                        .location(request.getLocation() != null ? request.getLocation() : UNKNOWN)
                        .gardenType(request.getGardenType())
                        .build())
                .build();
    }

    private PlantRecommendationResponse handleWebClientError(WebClientResponseException e) {
        log.error("OpenAI API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
        return PlantRecommendationResponse.builder()
                .success(false)
                .error("Error calling OpenAI API: " + e.getMessage())
                .build();
    }

    private PlantRecommendationResponse handleGenericError(Exception e) {
        log.error("Error generating plant recommendations: {}", e.getMessage(), e);
        return PlantRecommendationResponse.builder()
                .success(false)
                .error("Error generating recommendations: " + e.getMessage())
                .build();
    }
}
