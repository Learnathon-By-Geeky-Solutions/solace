package dev.solace.twiggle.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlantsLibrarySearchCriteria {
    private String commonName;
    private String otherName;
    private String scientificName;
    private String origin;
    private String plantType;
    private String climate;
    private String lifeCycle;
    private String wateringFrequency;
    private String soilType;
    private String size;
    private String sunlightRequirement;
    private String growthRate;
    private String idealPlace;
    private String careLevel;
    private String bestPlantingSeason;
    private Double timeToHarvest;
    private Boolean flower;
    private Boolean fruit;
    private Boolean medicinal;
}
