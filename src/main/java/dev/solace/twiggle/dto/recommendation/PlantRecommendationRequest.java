package dev.solace.twiggle.dto.recommendation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for plant recommendation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantRecommendationRequest {

    @NotBlank(message = "Garden type must not be blank")
    private String gardenType; // optional — default: "Any"

    @NotBlank(message = "Location must not be blank")
    private String location; // optional — default: "Any"

    // optional — can be empty
    private List<String> existingPlants;

    @Size(max = 500, message = "Message must not exceed 500 characters.")
    private String message; // optional — default: "Recommend plants"

    @Valid
    private UserPreferences userPreferences;

    /**
     * Apply fallback/default values for optional or blank fields.
     */
    public void applyDefaultsIfNeeded() {
        gardenType = defaultIfBlank(gardenType, "Any");
        location = defaultIfBlank(location, "Any");
        message = defaultIfBlank(message, "Recommend plants");
        existingPlants = existingPlants != null ? existingPlants : List.of();

        if (userPreferences == null) {
            userPreferences = createDefaultPreferences();
        } else {
            applyDefaultsToUserPreferences(userPreferences);
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private PlantRecommendationRequest.UserPreferences createDefaultPreferences() {
        return PlantRecommendationRequest.UserPreferences.builder()
                .experience("beginner")
                .harvestGoals(List.of())
                .timeCommitment("moderate")
                .build();
    }

    private void applyDefaultsToUserPreferences(PlantRecommendationRequest.UserPreferences prefs) {
        prefs.setExperience(defaultIfBlank(prefs.getExperience(), "beginner"));
        prefs.setHarvestGoals(prefs.getHarvestGoals() != null ? prefs.getHarvestGoals() : List.of());
        prefs.setTimeCommitment(defaultIfBlank(prefs.getTimeCommitment(), "moderate"));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferences {

        private String experience; // optional — default: "beginner"
        private List<String> harvestGoals; // optional — default: empty list
        private String timeCommitment; // optional — default: "moderate"
    }
}
