package dev.solace.twiggle.dto.recommendation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.solace.twiggle.util.StringToListDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Plant name is required.")
    private String name;

    @NotBlank(message = "Plant type is required.")
    private String type;

    @Size(max = 1000, message = "Description must be under 1000 characters.")
    private String description;

    private String sunlightRequirements;
    private String wateringFrequency;
    private String seasonalTips;

    @JsonDeserialize(using = StringToListDeserializer.class)
    private List<@NotBlank(message = "Companion plant name cannot be blank.") String> companionPlants;

    private String personalNote;
    private String difficulty;
    private String imageURL; // ✅ Correct field name for setter
}
