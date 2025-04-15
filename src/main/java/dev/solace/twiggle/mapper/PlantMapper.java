package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.model.Plant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlantMapper {

    /**
     * Converts a Plant entity to a PlantDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param plant the entity to convert
     * @return the corresponding DTO
     */
    PlantDTO toDto(Plant plant);

    /**
     * Converts a PlantDTO to a Plant entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param plantDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    Plant toEntity(PlantDTO plantDTO);
}
