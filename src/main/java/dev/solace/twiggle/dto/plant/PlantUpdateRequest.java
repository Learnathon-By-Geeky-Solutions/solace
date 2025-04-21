package dev.solace.twiggle.dto.plant;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing plant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantUpdateRequest {
    @Size(max = 100, message = "Common name cannot exceed 100 characters")
    private String commonName;

    @Size(max = 100, message = "Scientific name cannot exceed 100 characters")
    private String scientificName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 50, message = "Sunlight requirements cannot exceed 50 characters")
    private String sunlight;

    @Size(max = 50, message = "Watering frequency cannot exceed 50 characters")
    private String wateringFrequency;

    private String imageUrl;
    private String growthHabit;
    private String harvestTime;
    private String plantingTime;
    private String soilType;
    private Integer daysToMaturity;
    private String careInstructions;
}
