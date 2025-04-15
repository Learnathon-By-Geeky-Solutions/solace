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
public class ActivityDTO {

    @NotNull(message = "User ID is required") private UUID userId;

    private UUID gardenPlanId;

    @NotBlank(message = "Activity type is required")
    @Size(max = 255, message = "Activity type must be less than 255 characters")
    private String activityType;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    private OffsetDateTime createdAt;
}
