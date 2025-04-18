package dev.solace.twiggle.dto.plant;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning plant data to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantResponse {
    private UUID id;
    private String commonName;
    private String scientificName;
    private String description;
    private String sunlight;
    private String wateringFrequency;
    private String imageUrl;
    private String growthHabit;
    private String harvestTime;
    private String plantingTime;
    private String soilType;
    private Integer daysToMaturity;
    private String careInstructions;
}
