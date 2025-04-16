package dev.solace.twiggle.dto.plant;

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
    private String gardenType;
    private String location;
    private List<String> existingPlants;
    private String message;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferences {
        private String experience;
        private List<String> harvestGoals;
        private String timeCommitment;
    }
    
    private UserPreferences userPreferences;
} 