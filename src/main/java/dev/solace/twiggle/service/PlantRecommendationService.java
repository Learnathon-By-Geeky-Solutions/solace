package dev.solace.twiggle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.solace.twiggle.dto.recommendation.PlantRecommendation;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import dev.solace.twiggle.dto.recommendation.PlantRecommendationResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class PlantRecommendationService {

    private static final String UNKNOWN = "Unknown";
    private static final String CONTENT = "content";

    private final WebClient openaiWebClient;
    private final WebClient unsplashWebClient;
    private final ObjectMapper objectMapper;

    public PlantRecommendationService(
            WebClient openaiWebClient, WebClient unsplashWebClient, ObjectMapper objectMapper) {
        this.openaiWebClient = openaiWebClient;
        this.unsplashWebClient = unsplashWebClient;
        this.objectMapper = objectMapper;
    }

    public PlantRecommendationResponse getPlantRecommendations(PlantRecommendationRequest request) {
        logRequestDetails(request);
        setDefaultUserPreferencesIfAbsent(request);

        String currentSeason = getCurrentSeason(request.getLocation());
        log.info("Current season: {}", currentSeason);

        try {
            String openAiResponse = fetchRecommendationsFromOpenAI(request, currentSeason);
            List<PlantRecommendation> recommendations = parseRecommendationsFromJson(openAiResponse);
            recommendations.forEach(this::fetchPlantImage);

            return buildSuccessResponse(request, currentSeason, recommendations);
        } catch (WebClientResponseException e) {
            return handleWebClientError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }

    private void logRequestDetails(PlantRecommendationRequest request) {
        log.info("Getting plant recommendations for {} garden", request.getGardenType());
        log.info("Location: {}", Optional.ofNullable(request.getLocation()).orElse(UNKNOWN));
        log.info(
                "Existing plants: {}",
                Optional.ofNullable(request.getExistingPlants())
                        .map(list -> String.join(", ", list))
                        .orElse("None"));
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

    private String fetchRecommendationsFromOpenAI(PlantRecommendationRequest request, String season) {
        Map<String, Object> openAiRequest = new HashMap<>();
        openAiRequest.put("model", "gpt-4o-mini");

        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", CONTENT, buildSystemPrompt(season)),
                Map.of("role", "user", CONTENT, buildUserPrompt(request, season)));

        openAiRequest.put("messages", messages);

        return openaiWebClient
                .post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(openAiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private PlantRecommendationResponse buildSuccessResponse(
            PlantRecommendationRequest request, String season, List<PlantRecommendation> recommendations) {
        return PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(recommendations)
                .meta(PlantRecommendationResponse.MetaData.builder()
                        .season(season)
                        .location(Optional.ofNullable(request.getLocation()).orElse(UNKNOWN))
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

    private List<PlantRecommendation> parseRecommendationsFromJson(String openAiResponse)
            throws JsonProcessingException {
        JsonNode responseNode = objectMapper.readTree(openAiResponse);
        String rawContent = responseNode
                .path("choices")
                .path(0)
                .path("message")
                .path(CONTENT)
                .asText()
                .trim();

        String validJson = cleanAndRepairJson(rawContent);
        return objectMapper.readValue(validJson, new TypeReference<>() {});
    }

    private String cleanAndRepairJson(String content) throws JsonProcessingException {
        if (!content.startsWith("[")) {
            Matcher matcher =
                    Pattern.compile("\\[\\s*\\{.*?\\}\\s*\\]", Pattern.DOTALL).matcher(content);
            if (matcher.find()) {
                content = matcher.group(0);
            } else if (content.startsWith("{") && content.endsWith("}")) {
                content = "[" + content + "]";
            } else {
                throw new JsonProcessingException("Unable to extract valid JSON") {};
            }
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON, attempting to repair...");
            content = content.replaceAll(",\\s*]", "]").replaceAll(",\\s*}", "}");

            int openBraces = (int) content.chars().filter(ch -> ch == '{').count();
            int closeBraces = (int) content.chars().filter(ch -> ch == '}').count();
            int openBrackets = (int) content.chars().filter(ch -> ch == '[').count();
            int closeBrackets = (int) content.chars().filter(ch -> ch == ']').count();

            StringBuilder sb = new StringBuilder(content);
            while (closeBraces < openBraces) sb.append('}');
            while (closeBrackets < openBrackets) sb.append(']');

            return sb.toString();
        }
    }

    private void fetchPlantImage(PlantRecommendation recommendation) {
        try {
            String query = recommendation.getName() + " plant";
            String imageResponse = unsplashWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/photos")
                            .queryParam("query", query)
                            .queryParam("per_page", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (imageResponse != null) {
                JsonNode imageData = objectMapper.readTree(imageResponse);
                if (imageData.path("results").size() > 0) {
                    String imageUrl = imageData
                            .path("results")
                            .path(0)
                            .path("urls")
                            .path("small")
                            .asText();
                    recommendation.setImageURL(imageUrl);
                } else {
                    log.info("No image found for {}", recommendation.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching image for {}: {}", recommendation.getName(), e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String season) {
        StringBuilder prompt = new StringBuilder(256);
        prompt.append("You are a knowledgeable and warm gardening assistant...\n")
                .append("- seasonal_tips: Tips specific to the current season (")
                .append(season)
                .append(")\n")
                .append("- image_url: String - URL to a plant image (leave empty string)\n")
                .append("CRITICALLY IMPORTANT: Return ONLY valid, parseable JSON. ...");
        return prompt.toString();
    }

    private String buildUserPrompt(PlantRecommendationRequest request, String season) {
        var prefs = request.getUserPreferences();
        StringBuilder prompt = new StringBuilder(512);

        prompt.append("Garden information:\n")
                .append("- Type: ")
                .append(request.getGardenType())
                .append("\n")
                .append("- Location: ")
                .append(Optional.ofNullable(request.getLocation()).orElse(UNKNOWN))
                .append("\n")
                .append("- Current season: ")
                .append(season)
                .append("\n")
                .append("- Gardening experience: ")
                .append(Optional.ofNullable(prefs.getExperience()).orElse("beginner"))
                .append("\n")
                .append("- Time commitment: ")
                .append(Optional.ofNullable(prefs.getTimeCommitment()).orElse("moderate"))
                .append("\n")
                .append("- Harvest goals: ")
                .append(
                        prefs.getHarvestGoals() != null
                                        && !prefs.getHarvestGoals().isEmpty()
                                ? String.join(", ", prefs.getHarvestGoals())
                                : "general gardening")
                .append("\n")
                .append("- Existing plants: ")
                .append(
                        request.getExistingPlants() != null
                                        && !request.getExistingPlants().isEmpty()
                                ? String.join(", ", request.getExistingPlants())
                                : "None yet")
                .append("\n\n")
                .append("User query: ")
                .append(request.getMessage())
                .append("\n\n")
                .append("Please provide 3-5 personalized plant recommendations...");

        return prompt.toString();
    }

    private String getCurrentSeason(String location) {
        int month = Calendar.getInstance().get(Calendar.MONTH);
        boolean isSouthern = location != null && isSouthernHemisphereCountry(location.toLowerCase());

        return switch (month) {
            case 2, 3, 4 -> isSouthern ? "autumn" : "spring";
            case 5, 6, 7 -> isSouthern ? "winter" : "summer";
            case 8, 9, 10 -> isSouthern ? "spring" : "autumn";
            default -> isSouthern ? "summer" : "winter";
        };
    }

    /**
     * Checks if the location is in the Southern Hemisphere by looking for specific
     * country names.
     * Uses a safer approach than regex with potentially catastrophic backtracking.
     *
     * @param location The location string in lowercase
     * @return true if the location is in a Southern Hemisphere country
     */
    private boolean isSouthernHemisphereCountry(String location) {
        if (location == null) {
            return false;
        }

        String[] southernCountries = {"australia", "new zealand", "argentina", "chile", "south africa", "brazil"};

        for (String country : southernCountries) {
            if (location.contains(country)) {
                return true;
            }
        }

        return false;
    }
}
