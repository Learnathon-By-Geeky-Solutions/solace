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
public class GardenPlanDTO {

    @NotNull(message = "User ID is required") private UUID userId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Type is required")
    @Size(max = 255, message = "Type must be less than 255 characters")
    private String type;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @Size(max = 255, message = "Location must be less than 255 characters")
    private String location;

    @Size(max = 1000, message = "Thumbnail URL must be less than 1000 characters")
    private String thumbnailUrl;

    private Boolean isPublic;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
