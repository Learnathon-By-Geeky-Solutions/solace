package dev.solace.twiggle.dto.plant;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class DiseasePestListRequestDTO {
    private Long id;

    @Min(value = 1, message = "Page must be at least 1")
    private Integer page = 1;

    private String q;
}
