package dev.solace.twiggle.dto;

import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    @Size(max = 255, message = "Full name must be less than 255 characters")
    private String fullName;

    @Size(max = 1000, message = "Avatar URL must be less than 1000 characters")
    private String avatarUrl;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
