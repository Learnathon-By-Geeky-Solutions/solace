package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.GardenImageDTO;
import dev.solace.twiggle.model.GardenImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GardenImageMapper {

    /**
     * Converts a GardenImage entity to a GardenImageDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param gardenImage the entity to convert
     * @return the corresponding DTO
     */
    GardenImageDTO toDto(GardenImage gardenImage);

    /**
     * Converts a GardenImageDTO to a GardenImage entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param gardenImageDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    GardenImage toEntity(GardenImageDTO gardenImageDTO);
}
