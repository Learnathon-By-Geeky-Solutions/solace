package dev.solace.twiggle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantDTO {

    @NotNull(message = "Garden plan ID is required") private UUID gardenPlanId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Type is required")
    @Size(max = 255, message = "Type must be less than 255 characters")
    private String type;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    @Size(max = 100, message = "Watering frequency must be less than 100 characters")
    private String wateringFrequency;

    @Size(max = 100, message = "Sunlight requirements must be less than 100 characters")
    private String sunlightRequirements;

    private Integer positionX;

    private Integer positionY;

    @Size(max = 1000, message = "Image URL must be less than 1000 characters")
    private String imageUrl;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
