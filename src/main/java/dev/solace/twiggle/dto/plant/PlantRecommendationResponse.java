package dev.solace.twiggle.dto.plant;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object containing plant recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantRecommendationResponse {
    private boolean success;
    private List<PlantRecommendation> recommendations;
    private MetaData meta;
    private String error;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaData {
        private String season;
        private String location;
        private String gardenType;
    }
} 