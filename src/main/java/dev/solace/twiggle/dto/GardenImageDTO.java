package dev.solace.twiggle.dto;

import jakarta.validation.constraints.NotBlank;
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
public class GardenImageDTO {

    private UUID gardenPlanId;

    @NotBlank(message = "Image URL is required")
    @Size(max = 1000, message = "Image URL must be less than 1000 characters")
    private String imageUrl;

    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    private OffsetDateTime createdAt;
}
