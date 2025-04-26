package dev.solace.twiggle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantsLibraryDTO {
    private UUID id;

    @NotBlank(message = "Common name is required")
    @Size(max = 255, message = "Common name must be less than 255 characters")
    private String commonName;

    @Size(max = 255, message = "Other name must be less than 255 characters")
    private String otherName;

    @Size(max = 255, message = "Scientific name must be less than 255 characters")
    private String scientificName;

    @Size(max = 1000, message = "Short description must be less than 1000 characters")
    private String shortDescription;

    @Size(max = 255, message = "Origin must be less than 255 characters")
    private String origin;

    @Size(max = 100, message = "Plant type must be less than 100 characters")
    private String plantType;

    @Size(max = 100, message = "Climate must be less than 100 characters")
    private String climate;

    @Size(max = 100, message = "Life cycle must be less than 100 characters")
    private String lifeCycle;

    @Size(max = 100, message = "Watering frequency must be less than 100 characters")
    private String wateringFrequency;

    @Size(max = 255, message = "Soil type must be less than 255 characters")
    private String soilType;

    @Size(max = 100, message = "Size must be less than 100 characters")
    private String size;

    @Size(max = 100, message = "Sunlight requirement must be less than 100 characters")
    private String sunlightRequirement;

    @Size(max = 100, message = "Growth rate must be less than 100 characters")
    private String growthRate;

    @Size(max = 255, message = "Ideal place must be less than 255 characters")
    private String idealPlace;

    @Size(max = 100, message = "Care level must be less than 100 characters")
    private String careLevel;

    @Size(max = 1000, message = "Image URL must be less than 1000 characters")
    private String imageUrl;

    @Size(max = 100, message = "Best planting season must be less than 100 characters")
    private String bestPlantingSeason;

    @Size(max = 1000, message = "Gardening tips must be less than 1000 characters")
    private String gardeningTips;

    @Size(max = 1000, message = "Pruning guide must be less than 1000 characters")
    private String pruningGuide;

    private Double seedDepth;
    private Double germinationTime;
    private Double timeToHarvest;

    private Boolean flower;
    private Boolean fruit;
    private Boolean medicinal;

    // For the Range type, we'll use a simple DTO representation
    private Double minTemperature;
    private Double maxTemperature;

    private List<String> commonPests;
    private List<String> commonDiseases;
    private List<String> companionPlants;
    private List<String> avoidPlantingWith;
    private List<String> pestDiseasePreventionTips;
    private List<String> coolFacts;
    private List<String> edibleParts;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
