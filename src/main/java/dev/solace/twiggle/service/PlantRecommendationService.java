package dev.solace.twiggle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.solace.twiggle.dto.plant.PlantRecommendation;
import dev.solace.twiggle.dto.plant.PlantRecommendationRequest;
import dev.solace.twiggle.dto.plant.PlantRecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlantRecommendationService {

    private final WebClient openaiWebClient;
    private final WebClient unsplashWebClient;
    private final ObjectMapper objectMapper;

    /**
     * Gets plant recommendations based on the user's request
     * 
     * @param request the recommendation request parameters
     * @return a response containing plant recommendations
     */
    public PlantRecommendationResponse getPlantRecommendations(PlantRecommendationRequest request) {
        log.info("Getting plant recommendations for {} garden", request.getGardenType());
        log.info("Location: {}", request.getLocation() != null ? request.getLocation() : "Unknown");
        log.info("Existing plants: {}", request.getExistingPlants() != null ? String.join(", ", request.getExistingPlants()) : "None");
        log.info("User message: {}", request.getMessage());
        
        // Set default user preferences if not provided
        if (request.getUserPreferences() == null) {
            request.setUserPreferences(
                PlantRecommendationRequest.UserPreferences.builder()
                    .experience("beginner")
                    .harvestGoals(new ArrayList<>())
                    .timeCommitment("moderate")
                    .build()
            );
        }
        
        // Get current season for the user's location
        String currentSeason = getCurrentSeason(request.getLocation());
        log.info("Current season: {}", currentSeason);
        
        try {
            // Prepare request to OpenAI API
            Map<String, Object> openAiRequest = new HashMap<>();
            openAiRequest.put("model", "gpt-4o-mini");
            
            List<Map<String, Object>> messages = new ArrayList<>();
            
            // System message
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", buildSystemPrompt(currentSeason));
            messages.add(systemMessage);
            
            // User message
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", buildUserPrompt(request, currentSeason));
            messages.add(userMessage);
            
            openAiRequest.put("messages", messages);
            
            // Call OpenAI
            String openAiResponse = openaiWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(openAiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            // Parse the response
            JsonNode responseNode = objectMapper.readTree(openAiResponse);
            String content = responseNode.path("choices").path(0).path("message").path("content").asText();
            
            List<PlantRecommendation> recommendations = parseRecommendationsFromJson(content);
            
            // Fetch images for the recommendations
            for (PlantRecommendation recommendation : recommendations) {
                fetchPlantImage(recommendation);
            }
            
            // Build the response
            return PlantRecommendationResponse.builder()
                .success(true)
                .recommendations(recommendations)
                .meta(PlantRecommendationResponse.MetaData.builder()
                    .season(currentSeason)
                    .location(request.getLocation() != null ? request.getLocation() : "Unknown")
                    .gardenType(request.getGardenType())
                    .build())
                .build();
                
        } catch (WebClientResponseException e) {
            log.error("OpenAI API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return PlantRecommendationResponse.builder()
                .success(false)
                .error("Error calling OpenAI API: " + e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("Error generating plant recommendations: {}", e.getMessage(), e);
            return PlantRecommendationResponse.builder()
                .success(false)
                .error("Error generating recommendations: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Parses the JSON response from OpenAI into a list of plant recommendations
     */
    private List<PlantRecommendation> parseRecommendationsFromJson(String content) throws JsonProcessingException {
        List<PlantRecommendation> recommendations = null;
        
        // Log the raw content for debugging
        log.debug("Raw content from OpenAI: {}", content);
        
        // First try to clean up the JSON if needed
        String cleanedContent = content.trim();
        
        // Make sure we have an array
        if (!cleanedContent.startsWith("[")) {
            // Try to extract array using regex
            String arrayPattern = "\\[\\s*\\{.*\\}\\s*\\]";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(arrayPattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = pattern.matcher(cleanedContent);
            
            if (matcher.find()) {
                cleanedContent = matcher.group(0);
                log.debug("Extracted JSON array using regex: {}", cleanedContent);
            } else {
                log.warn("Could not find JSON array in content, attempting to wrap content in array");
                // If no array found, try to identify a single JSON object and wrap it
                if (cleanedContent.startsWith("{") && cleanedContent.endsWith("}")) {
                    cleanedContent = "[" + cleanedContent + "]";
                } else {
                    throw new JsonProcessingException("Unable to extract valid JSON array or object from response") {};
                }
            }
        }
        
        // Ensure the JSON is well-formed
        try {
            // Validate JSON structure
            JsonNode jsonNode = objectMapper.readTree(cleanedContent);
            // Convert validated JSON back to string
            cleanedContent = objectMapper.writeValueAsString(jsonNode);
            log.debug("Validated and formatted JSON: {}", cleanedContent);
        } catch (JsonProcessingException e) {
            log.warn("JSON validation failed, attempting to repair: {}", e.getMessage());
            // JSON is invalid, try a more aggressive repair
            try {
                // Try to fix common JSON errors like trailing commas
                cleanedContent = cleanedContent.replaceAll(",\\s*\\]", "]");
                cleanedContent = cleanedContent.replaceAll(",\\s*\\}", "}");
                
                // Ensure all objects are closed properly
                int openBraces = countOccurrences(cleanedContent, '{');
                int closeBraces = countOccurrences(cleanedContent, '}');
                int openBrackets = countOccurrences(cleanedContent, '[');
                int closeBrackets = countOccurrences(cleanedContent, ']');
                
                // Add missing closing braces if needed
                while (closeBraces < openBraces) {
                    cleanedContent += "}";
                    closeBraces++;
                }
                
                // Add missing closing brackets if needed
                while (closeBrackets < openBrackets) {
                    cleanedContent += "]";
                    closeBrackets++;
                }
                
                log.debug("Repaired JSON: {}", cleanedContent);
            } catch (Exception ex) {
                log.error("Failed to repair JSON: {}", ex.getMessage());
                throw e; // Rethrow the original exception
            }
        }
        
        try {
            // Try to parse the content with a standard ObjectMapper first
            recommendations = objectMapper.readValue(cleanedContent, new TypeReference<List<PlantRecommendation>>() {});
        } catch (JsonProcessingException e) {
            log.debug("Standard parsing failed: {}", e.getMessage());
            
            // Try parsing with more lenient settings
            ObjectMapper lenientMapper = new ObjectMapper();
            lenientMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            lenientMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            lenientMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            lenientMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_MISSING_VALUES, true);
            lenientMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
            
            try {
                recommendations = lenientMapper.readValue(cleanedContent, new TypeReference<List<PlantRecommendation>>() {});
            } catch (JsonProcessingException ex) {
                log.error("Failed to parse JSON recommendation even with lenient settings: {}", cleanedContent, ex);
                throw ex;
            }
        }
        
        return recommendations != null ? recommendations : new ArrayList<>();
    }
    
    /**
     * Utility method to count occurrences of a character in a string
     */
    private int countOccurrences(String str, char character) {
        return (int) str.chars().filter(ch -> ch == character).count();
    }
    
    /**
     * Fetches a plant image from Unsplash and updates the recommendation
     */
    private void fetchPlantImage(PlantRecommendation recommendation) {
        try {
            String query = recommendation.getName() + " plant";
            String imageResponse = unsplashWebClient.get()
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
                    String imageUrl = imageData.path("results").path(0).path("urls").path("small").asText();
                    recommendation.setImage_url(imageUrl);
                } else {
                    log.info("No image found for {}", recommendation.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching image for {}: {}", recommendation.getName(), e.getMessage(), e);
        }
    }
    
    /**
     * Builds the system prompt for OpenAI
     */
    private String buildSystemPrompt(String currentSeason) {
        return "You are a knowledgeable and warm gardening assistant helping a user plan their garden. "
            + "Make recommendations feel personal and intimate, as if coming from an experienced gardener friend.\n\n"
            + "Provide plant recommendations in JSON format based on the user's garden type, location, experience level, and existing plants.\n"
            + "Format your response as a JSON array of plant objects with these exact fields and data types:\n"
            + "- name: String - Plant name\n"
            + "- type: String - vegetable/herb/flower/etc\n"
            + "- description: String - Personal, warm description of the plant addressing the user directly\n"
            + "- sunlight_requirements: String - full sun/partial shade/shade\n"
            + "- watering_frequency: String - daily/twice a week/weekly\n"
            + "- seasonal_tips: String - Tips specific to the current season (" + currentSeason + ")\n"
            + "- companion_plants: Array of strings - Plants that grow well with this one\n"
            + "- personal_note: String - A friendly, encouraging note about growing this plant\n"
            + "- difficulty: String - easy/moderate/challenging\n"
            + "- image_url: String - URL to a plant image (leave empty string)\n\n"
            + "CRITICALLY IMPORTANT: Return ONLY valid, parseable JSON. Do not include any text before or after the JSON array. "
            + "Ensure proper JSON formatting with all quotes, commas, and brackets properly placed."
            + "Make sure companion_plants is always an array of strings, even if there's only one companion plant. Example: [\"Basil\"] not \"Basil\"."
            + "Do not include trailing commas in arrays or objects."
            + "Ensure all JSON objects and arrays are properly closed."
            + "Example format: [{\"name\":\"Tomato\",...}, {\"name\":\"Basil\",...}]";
    }
    
    /**
     * Builds the user prompt for OpenAI
     */
    private String buildUserPrompt(PlantRecommendationRequest request, String currentSeason) {
        PlantRecommendationRequest.UserPreferences prefs = request.getUserPreferences();
        
        return "Garden information:\n"
            + "- Type: " + request.getGardenType() + "\n"
            + "- Location: " + (request.getLocation() != null ? request.getLocation() : "Unknown") + "\n"
            + "- Current season: " + currentSeason + "\n"
            + "- Gardening experience: " + (prefs.getExperience() != null ? prefs.getExperience() : "beginner") + "\n"
            + "- Time commitment: " + (prefs.getTimeCommitment() != null ? prefs.getTimeCommitment() : "moderate") + "\n"
            + "- Harvest goals: " + (prefs.getHarvestGoals() != null && !prefs.getHarvestGoals().isEmpty() 
                ? String.join(", ", prefs.getHarvestGoals()) : "general gardening") + "\n"
            + "- Existing plants: " + (request.getExistingPlants() != null && !request.getExistingPlants().isEmpty() 
                ? String.join(", ", request.getExistingPlants()) : "None yet") + "\n\n"
            + "User query: " + request.getMessage() + "\n\n"
            + "Please provide 3-5 personalized plant recommendations for this garden that would make the user feel "
            + "like they're getting advice from a friendly expert gardener.";
    }
    
    /**
     * Determines the current season based on location and month
     * 
     * @param location the user's location
     * @return the current season (spring, summer, autumn, winter)
     */
    private String getCurrentSeason(String location) {
        // Default to northern hemisphere
        int month = Calendar.getInstance().get(Calendar.MONTH);
        
        // Check if location contains southern hemisphere indicators
        boolean isSouthernHemisphere = location != null && 
            location.toLowerCase().matches(".*(australia|new zealand|argentina|chile|south africa|brazil).*");
        
        if (isSouthernHemisphere) {
            // Southern hemisphere seasons
            if (month >= 2 && month <= 4) return "autumn";
            if (month >= 5 && month <= 7) return "winter";
            if (month >= 8 && month <= 10) return "spring";
            return "summer";
        } else {
            // Northern hemisphere seasons
            if (month >= 2 && month <= 4) return "spring";
            if (month >= 5 && month <= 7) return "summer";
            if (month >= 8 && month <= 10) return "autumn";
            return "winter";
        }
    }
} 