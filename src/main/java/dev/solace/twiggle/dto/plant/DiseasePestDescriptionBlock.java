package dev.solace.twiggle.dto.plant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiseasePestDescriptionBlock {
    private String subtitle;
    private String description;
}
