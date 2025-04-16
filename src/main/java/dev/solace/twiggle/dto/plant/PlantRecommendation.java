package dev.solace.twiggle.dto.plant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.solace.twiggle.util.StringToListDeserializer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a plant recommendation from the OpenAI API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantRecommendation {
    private String name;
    private String type;
    private String description;
    private String sunlight_requirements;
    private String watering_frequency;
    private String seasonal_tips;
    
    @JsonDeserialize(using = StringToListDeserializer.class)
    private List<String> companion_plants;
    
    private String personal_note;
    private String difficulty;
    private String image_url;
} 