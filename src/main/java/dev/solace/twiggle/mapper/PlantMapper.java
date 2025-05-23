package dev.solace.twiggle.mapper;

import dev.solace.twiggle.dto.PlantDTO;
import dev.solace.twiggle.dto.plant.PlantCreateRequest;
import dev.solace.twiggle.dto.plant.PlantResponse;
import dev.solace.twiggle.dto.plant.PlantUpdateRequest;
import dev.solace.twiggle.model.Plant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
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

    /**
     * Converts a Plant entity to a PlantResponse DTO.
     *
     * @param plant the entity to convert
     * @return the corresponding PlantResponse DTO
     */
    PlantResponse toPlantResponse(Plant plant);

    /**
     * Converts a PlantCreateRequest DTO to a Plant entity.
     * Ignores the ID as it will be generated.
     *
     * @param request the PlantCreateRequest DTO
     * @return the corresponding Plant entity
     */
    @Mapping(target = "id", ignore = true)
    Plant toPlant(PlantCreateRequest request);

    /**
     * Updates an existing Plant entity from a PlantUpdateRequest DTO.
     * Ignores the ID from the request.
     *
     * @param request the PlantUpdateRequest DTO containing updates
     * @param plant   the target Plant entity to update
     */
    @Mapping(target = "id", ignore = true)
    void updatePlantFromRequest(PlantUpdateRequest request, @MappingTarget Plant plant);
}
