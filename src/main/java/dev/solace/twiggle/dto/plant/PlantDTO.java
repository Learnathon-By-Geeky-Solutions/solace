package dev.solace.twiggle.dto.plant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantDTO {
    private Long id;

    @JsonProperty("common_name")
    private String commonName;

    @JsonProperty("scientific_name")
    private List<String> scientificName;

    @JsonProperty("other_name")
    private List<String> otherNames;

    private String cycle;

    private String watering;

    private List<String> sunlight;

    private String family;

    private Boolean hybrid;

    private String authority;

    private String subspecies;

    private String cultivar;

    private String variety;

    @JsonProperty("species_epithet")
    private String speciesEpithet;

    private String genus;

    @JsonProperty("default_image")
    private PlantImageDTO defaultImage;
}
