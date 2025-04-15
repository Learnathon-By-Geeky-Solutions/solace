package dev.solace.twiggle.dto;

import jakarta.validation.constraints.NotNull;
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
public class ImageLikeDTO {

    @NotNull(message = "Image ID is required") private UUID imageId;

    @NotNull(message = "User ID is required") private UUID userId;

    private OffsetDateTime createdAt;
}
