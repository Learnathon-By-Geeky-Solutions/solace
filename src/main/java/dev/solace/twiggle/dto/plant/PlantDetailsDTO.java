package dev.solace.twiggle.dto.plant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantDetailsDTO {
    private Long id;

    @JsonProperty("common_name")
    private String commonName;

    @JsonProperty("scientific_name")
    private List<String> scientificName;

    @JsonProperty("other_name")
    private List<String> otherNames;

    private String description;
    private String family;
    private String type;
    private String cycle;
    private String watering;
    private String growthRate;
    private String maintenance;
    private String careLevel;

    @JsonProperty("watering_period")
    private String wateringPeriod;

    @JsonProperty("watering_general_benchmark")
    private Object wateringGeneralBenchmark;

    @JsonProperty("sunlight")
    private List<String> sunlight;

    @JsonProperty("pruning_month")
    private List<String> pruningMonth;

    @JsonProperty("pruning_count")
    private List<Object> pruningCount; // Fixed type mismatch

    @JsonProperty("hardiness")
    private Map<String, String> hardiness;

    @JsonProperty("hardiness_location")
    private Map<String, String> hardinessLocation;

    @JsonProperty("dimension")
    private String dimension;

    @JsonProperty("dimensions")
    private List<Map<String, Object>> dimensions; // Updated from Object

    @JsonProperty("indoor")
    private Boolean indoor;

    private List<String> pest;
    private List<String> diseases;

    @JsonProperty("cones")
    private Boolean cones;

    @JsonProperty("fruits")
    private Boolean fruits;

    @JsonProperty("edible_fruit")
    private Boolean edibleFruit;

    @JsonProperty("edible_fruit_taste_profile")
    private String edibleFruitTasteProfile;

    @JsonProperty("fruit_nutritional_value")
    private String fruitNutritionalValue;

    @JsonProperty("fruit_color")
    private List<String> fruitColor;

    @JsonProperty("fruiting_months")
    private List<String> fruitingMonths;

    @JsonProperty("flowers")
    private Boolean flowers;

    @JsonProperty("flowering_description")
    private String floweringDescription;

    @JsonProperty("flower_color")
    private Object flowerColor;

    @JsonProperty("flowering_months")
    private List<String> floweringMonths;

    @JsonProperty("attracts")
    private List<String> attracts;

    @JsonProperty("propagation")
    private List<String> propagation;

    @JsonProperty("edible_leaf")
    private Boolean edibleLeaf;

    @JsonProperty("leaf_color")
    private List<String> leafColor;

    @JsonProperty("edible_leaf_taste_profile")
    private String edibleLeafTasteProfile;

    @JsonProperty("leaf_nutritional_value")
    private String leafNutritionalValue;

    @JsonProperty("medicinal")
    private Boolean medicinal;

    @JsonProperty("poisonous_to_humans")
    private Boolean poisonousToHumans;

    @JsonProperty("poisonous_to_pets")
    private Boolean poisonousToPets;

    @JsonProperty("drought_tolerant")
    private Boolean droughtTolerant;

    @JsonProperty("salt_tolerant")
    private Boolean saltTolerant;

    @JsonProperty("thorny")
    private Boolean thorny;

    @JsonProperty("invasive")
    private Boolean invasive;

    @JsonProperty("rare")
    private Boolean rare;

    @JsonProperty("rare_level")
    private String rareLevel;

    @JsonProperty("tropical")
    private Boolean tropical;

    @JsonProperty("cuisine")
    private Boolean cuisine;

    @JsonProperty("default_image")
    private PlantImageDTO defaultImage;

    @JsonProperty("other_images")
    private String images; // Changed from List<PlantImageDTO> to String

    @JsonProperty("plant_anatomy")
    private List<Map<String, Object>> plantAnatomy;

    private List<String> soil;
    private Object seeds;

    @JsonProperty("flowering_season")
    private String floweringSeason;

    @JsonProperty("fruiting_season")
    private String fruitingSeason;

    @JsonProperty("harvest_season")
    private String harvestSeason;

    @JsonProperty("harvest_method")
    private String harvestMethod;

    private Boolean leaf;

    @JsonProperty("pest_susceptibility")
    private List<Object> pestSusceptibility;

    @JsonProperty("care_guides")
    private String careGuides; // Newly added
}
