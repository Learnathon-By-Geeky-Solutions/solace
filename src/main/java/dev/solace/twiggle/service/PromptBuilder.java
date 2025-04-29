package dev.solace.twiggle.service;

import dev.solace.twiggle.dto.recommendation.PlantRecommendationRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {
    private static final String UNKNOWN = "Unknown";

    public String buildSystemPrompt(String season) {
        return new StringBuilder(256)
                .append("You are a knowledgeable and warm gardening assistant...\n")
                .append("- seasonal_tips: Tips specific to the current season (")
                .append(season)
                .append(")\n")
                .append("- image_url: String - URL to a plant image (leave empty string)\n")
                .append("CRITICALLY IMPORTANT: Return ONLY valid, parseable JSON. ...")
                .toString();
    }

    public String buildUserPrompt(PlantRecommendationRequest request, String season) {
        var prefs = request.getUserPreferences();

        return new StringBuilder(512)
                .append("Garden information:\n")
                .append("- Type: ")
                .append(request.getGardenType())
                .append("\n")
                .append("- Location: ")
                .append(request.getLocation() != null ? request.getLocation() : UNKNOWN)
                .append("\n")
                .append("- Current season: ")
                .append(season)
                .append("\n")
                .append("- Gardening experience: ")
                .append(prefs.getExperience() != null ? prefs.getExperience() : "beginner")
                .append("\n")
                .append("- Time commitment: ")
                .append(prefs.getTimeCommitment() != null ? prefs.getTimeCommitment() : "moderate")
                .append("\n")
                .append("- Harvest goals: ")
                .append(formatHarvestGoals(prefs))
                .append("\n")
                .append("- Existing plants: ")
                .append(formatExistingPlants(request))
                .append("\n\n")
                .append("User query: ")
                .append(request.getMessage())
                .append("\n\n")
                .append("Please provide 3-5 personalized plant recommendations...")
                .toString();
    }

    private String formatHarvestGoals(PlantRecommendationRequest.UserPreferences prefs) {
        return prefs.getHarvestGoals() != null && !prefs.getHarvestGoals().isEmpty()
                ? String.join(", ", prefs.getHarvestGoals())
                : "general gardening";
    }

    private String formatExistingPlants(PlantRecommendationRequest request) {
        return request.getExistingPlants() != null
                        && !request.getExistingPlants().isEmpty()
                ? String.join(", ", request.getExistingPlants())
                : "None yet";
    }
}
