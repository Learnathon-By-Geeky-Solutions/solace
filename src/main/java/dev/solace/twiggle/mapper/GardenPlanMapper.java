package dev.solace.twiggle.mapper;

import dev.solace.twiggle.model.GardenPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import dev.solace.twiggle.dto.GardenPlanDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GardenPlanMapper {

    /**
     * Converts a GardenPlan entity to a GardenPlanDTO.
     * ID is not mapped as it's auto-generated.
     *
     * @param gardenPlan the entity to convert
     * @return the corresponding DTO
     */
    GardenPlanDTO toDto(GardenPlan gardenPlan);

    /**
     * Converts a GardenPlanDTO to a GardenPlan entity.
     * ID will be auto-generated and not set from the DTO.
     *
     * @param gardenPlanDTO the DTO to convert
     * @return the corresponding entity
     */
    @Mapping(target = "id", ignore = true)
    GardenPlan toEntity(GardenPlanDTO gardenPlanDTO);
}