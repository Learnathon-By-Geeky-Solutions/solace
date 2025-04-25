package dev.solace.twiggle.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPlantDTO {

    @NotNull(message = "Garden plan ID is required") private UUID gardenPlanId;

    @NotNull(message = "Plants library ID is required") private UUID plantsLibraryId;
}
