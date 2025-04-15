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
public class ImageCommentDTO {

    @NotNull(message = "Image ID is required") private UUID imageId;

    @NotNull(message = "User ID is required") private UUID userId;

    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    private String comment;

    private OffsetDateTime createdAt;
}
