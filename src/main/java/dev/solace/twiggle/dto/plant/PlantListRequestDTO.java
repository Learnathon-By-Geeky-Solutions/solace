package dev.solace.twiggle.dto.plant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PlantListRequestDTO {
    @Min(value = 1, message = "Page must be at least 1")
    private Integer page = 1;

    private String q;

    @Pattern(regexp = "^(asc|desc)$", message = "Order must be either 'asc' or 'desc'")
    private String order;

    private Boolean edible;

    private Boolean poisonous;

    @Pattern(
            regexp = "^(perennial|annual|biennial|biannual)$",
            message = "Cycle must be one of: perennial, annual, biennial, biannual")
    private String cycle;

    @Pattern(
            regexp = "^(frequent|average|minimum|none)$",
            message = "Watering must be one of: frequent, average, minimum, none")
    private String watering;

    @Pattern(
            regexp = "^(full_shade|part_shade|sun-part_shade|full_sun)$",
            message = "Sunlight must be one of: full_shade, part_shade, sun-part_shade, full_sun")
    private String sunlight;

    private Boolean indoor;

    @Min(value = 1, message = "Hardiness min value is 1")
    @Max(value = 13, message = "Hardiness max value is 13")
    private Integer hardiness;
}
