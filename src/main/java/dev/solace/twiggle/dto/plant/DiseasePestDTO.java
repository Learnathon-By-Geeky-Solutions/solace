package dev.solace.twiggle.dto.plant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiseasePestDTO {
    private Long id;

    @JsonProperty("common_name")
    private String commonName;

    @JsonProperty("scientific_name")
    private String scientificName;

    @JsonProperty("other_name")
    private List<String> otherNames;

    private String family;
    private String type;

    // 🔁 Was String — now List<DiseasePestDescriptionBlock>
    private List<DiseasePestDescriptionBlock> description;

    private List<DiseasePestDescriptionBlock> solution;

    @JsonProperty("host")
    private List<String> hostPlants;

    @JsonProperty("images")
    private List<DiseasePestImageDTO> images;
}
